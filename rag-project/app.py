import os
import sys

import streamlit as st

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from rag_core.rag_chain import RAGChat, DirectDB


def try_init_rag() -> RAGChat | None:
    try:
        rag = RAGChat()
        if rag.is_available():
            return rag
        return None
    except Exception:
        return None


def try_connect_db() -> tuple[bool, str]:
    try:
        db = DirectDB()
        tables = db.db.get_tables()
        return True, f"已连接，共 {len(tables)} 个数据表"
    except Exception as e:
        return False, str(e)


def main():
    st.set_page_config(
        page_title="图书智能助手",
        page_icon="🤖",
        layout="wide",
    )

    # 从 URL 参数读取配置（由前端 iframe 传入）
    query_params = st.query_params
    user_id = query_params.get("user_id", [""])
    if isinstance(user_id, list):
        user_id = user_id[0] if user_id else ""
    if "user_id" not in st.session_state:
        st.session_state.user_id = user_id
    # 如果 URL 中的 user_id 变了，清空会话状态重新加载
    elif st.session_state.user_id != user_id:
        st.session_state.user_id = user_id
        for key in list(st.session_state.keys()):
            if key != "user_id":
                del st.session_state[key]

    if "rag" not in st.session_state:
        rag = try_init_rag()
        if rag is None:
            st.error("❌ 无法初始化 LLM，请检查环境变量 DASHSCOPE_API_KEY 是否已配置")
            st.info("配置方式：set DASHSCOPE_API_KEY=your_api_key")
            st.stop()
        st.session_state.rag = rag

    db_ok, db_msg = try_connect_db()
    if not db_ok:
        st.error(f"❌ 无法连接数据库: {db_msg}")
        st.stop()

    if "current_session_id" not in st.session_state:
        uid = st.session_state.user_id
        sessions = st.session_state.rag.memory.get_sessions(uid)
        if sessions:
            st.session_state.current_session_id = sessions[0]["id"]
        else:
            st.session_state.current_session_id = None

    with st.sidebar:
        st.title("📚 图书智能助手")
        st.divider()

        st.subheader("💬 对话列表")

        sessions = st.session_state.rag.memory.get_sessions(st.session_state.user_id)

        col1, col2 = st.columns([3, 1])
        with col1:
            if st.button("＋ 新建对话", use_container_width=True):
                sid = st.session_state.rag.memory.create_session("新对话", st.session_state.user_id)
                st.session_state.current_session_id = sid
                st.rerun()
        with col2:
            if st.button("🗑️", help="删除当前对话"):
                if sessions:
                    current = st.session_state.current_session_id
                    st.session_state.rag.memory.delete_session(current)
                    remaining = st.session_state.rag.memory.get_sessions(st.session_state.user_id)
                    if remaining:
                        st.session_state.current_session_id = remaining[0]["id"]
                    else:
                        st.session_state.current_session_id = None
                    st.rerun()

        for session in sessions:
            is_active = session["id"] == st.session_state.current_session_id
            label = f"{'👉 ' if is_active else '   '}{session['name']} ({session['message_count']}条)"
            if st.button(
                label,
                key=f"session_{session['id']}",
                use_container_width=True,
                type="primary" if is_active else "secondary",
            ):
                st.session_state.current_session_id = session["id"]
                st.rerun()

        st.divider()
        st.caption(f"✅ {db_msg}")
        st.caption(f"📝 共 {len(sessions)} 个对话")

    chat_placeholder = st.container()
    with chat_placeholder:
        st.header("💬 对话")

        if st.session_state.current_session_id is None:
            st.info("👋 点击左侧「＋ 新建对话」开始提问")
        else:
            messages = st.session_state.rag.memory.get_messages(
                st.session_state.current_session_id
            )
            for msg in messages:
                with st.chat_message(msg["role"]):
                    cols = st.columns([10, 1])
                    with cols[0]:
                        st.markdown(msg["content"])
                    if msg["role"] == "assistant":
                        with cols[1]:
                            if st.button("✕", key=f"del_{msg['id']}", help="删除此条问答"):
                                st.session_state.rag.memory.delete_pair_by_msg_id(msg["id"])
                                st.rerun()

    if prompt := st.chat_input("请输入您的问题，例如：有哪些Java相关的书籍？"):
        # 如果没有当前会话，自动创建一个
        if st.session_state.current_session_id is None:
            sid = st.session_state.rag.memory.create_session("新对话", st.session_state.user_id)
            st.session_state.current_session_id = sid
            st.rerun()
        with st.chat_message("user"):
            st.markdown(prompt)

        with st.chat_message("assistant"):
            with st.spinner("正在查询数据库并思考..."):
                try:
                    answer = st.session_state.rag.ask(
                        prompt,
                        st.session_state.current_session_id,
                        st.session_state.user_id,
                    )
                    st.markdown(answer)
                except Exception as e:
                    error_msg = f"出错了: {str(e)}"
                    import traceback
                    st.error(error_msg)
                    with st.expander("错误详情"):
                        st.code(traceback.format_exc())
                    st.session_state.rag.memory.add_message(
                        st.session_state.current_session_id, "assistant", error_msg
                    )

        st.rerun()


if __name__ == "__main__":
    main()
