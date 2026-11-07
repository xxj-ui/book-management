import os
import sys
import sqlite3
import uuid
from datetime import datetime
from typing import Optional

from dotenv import load_dotenv
from langchain_community.chat_models import ChatOpenAI
from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import HumanMessage

sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "mcp_server"))
from config import DATABASE_URL
from server import DatabaseManager

load_dotenv()

LLM_API_BASE = "https://api.deepseek.com/v1"
DB_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MEMORY_DB_PATH = os.path.join(DB_DIR, "chat_history.db")
# 学期起始日（第1周周一），由"第11周周三=2026-05-13"反推得出
SEMESTER_START = "2026-03-02"
CURRENT_SEMESTER = "2025-2026-2"


def get_time_context() -> str:
    today = datetime.now()
    weekday_map = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"]
    weekday_str = weekday_map[today.weekday()]
    
    start = datetime.strptime(SEMESTER_START, "%Y-%m-%d")
    days_diff = (today - start).days
    week_num = days_diff // 7 + 1
    if week_num < 1:
        week_num = 1
    
    return f"当前时间：{CURRENT_SEMESTER}学期，第{week_num}周，{weekday_str}（{today.strftime('%Y年%m月%d日')}）"


class PersistentMemory:
    def __init__(self, db_path: str = MEMORY_DB_PATH):
        self.db_path = db_path
        self._init_db()

    def _init_db(self):
        with sqlite3.connect(self.db_path) as conn:
            conn.execute("""
                CREATE TABLE IF NOT EXISTS sessions (
                    id TEXT PRIMARY KEY,
                    user_id TEXT DEFAULT '',
                    name TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            """)
            conn.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id TEXT NOT NULL,
                    role TEXT NOT NULL,
                    content TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (session_id) REFERENCES sessions(id)
                )
            """)
            conn.commit()

            # 迁移：旧表没有 user_id 列时补充
            try:
                conn.execute("ALTER TABLE sessions ADD COLUMN user_id TEXT DEFAULT ''")
                conn.commit()
            except sqlite3.OperationalError:
                pass  # 列已存在

    def create_session(self, name: str = "新对话", user_id: str = "") -> str:
        session_id = str(uuid.uuid4())[:8]
        now = datetime.now().isoformat()
        with sqlite3.connect(self.db_path) as conn:
            conn.execute(
                "INSERT INTO sessions (id, user_id, name, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                (session_id, user_id, name, now, now),
            )
            conn.commit()
        return session_id

    def get_sessions(self, user_id: str = "") -> list[dict]:
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            rows = conn.execute(
                "SELECT id, name, created_at, updated_at FROM sessions WHERE user_id = ? ORDER BY updated_at DESC",
                (user_id,),
            ).fetchall()
            result = []
            for row in rows:
                session = dict(row)
                count = conn.execute(
                    "SELECT COUNT(*) as cnt FROM messages WHERE session_id = ?",
                    (session["id"],),
                ).fetchone()["cnt"]
                session["message_count"] = count
                result.append(session)
            return result

    def delete_session(self, session_id: str):
        with sqlite3.connect(self.db_path) as conn:
            conn.execute("DELETE FROM messages WHERE session_id = ?", (session_id,))
            conn.execute("DELETE FROM sessions WHERE id = ?", (session_id,))
            conn.commit()

    def rename_session(self, session_id: str, new_name: str):
        now = datetime.now().isoformat()
        with sqlite3.connect(self.db_path) as conn:
            conn.execute(
                "UPDATE sessions SET name = ?, updated_at = ? WHERE id = ?",
                (new_name, now, session_id),
            )
            conn.commit()

    def get_messages(self, session_id: str) -> list[dict]:
        with sqlite3.connect(self.db_path) as conn:
            conn.row_factory = sqlite3.Row
            rows = conn.execute(
                "SELECT id, role, content, created_at FROM messages WHERE session_id = ? ORDER BY id",
                (session_id,),
            ).fetchall()
            return [dict(r) for r in rows]

    def delete_pair_by_msg_id(self, msg_id: int):
        """删除指定消息及其上一条消息（一对问答）"""
        with sqlite3.connect(self.db_path) as conn:
            # 获取本条和上一条的 id
            rows = conn.execute(
                "SELECT id FROM messages WHERE id <= ? ORDER BY id DESC LIMIT 2",
                (msg_id,),
            ).fetchall()
            ids = [r[0] for r in rows]
            if ids:
                conn.execute(
                    f"DELETE FROM messages WHERE id IN ({','.join('?' * len(ids))})",
                    ids,
                )
            conn.commit()

    def add_message(self, session_id: str, role: str, content: str):
        now = datetime.now().isoformat()
        with sqlite3.connect(self.db_path) as conn:
            conn.execute(
                "INSERT INTO messages (session_id, role, content, created_at) VALUES (?, ?, ?, ?)",
                (session_id, role, content, now),
            )
            conn.execute(
                "UPDATE sessions SET updated_at = ? WHERE id = ?",
                (now, session_id),
            )
            conn.commit()

    def delete_last_pair(self, session_id: str):
        """删除当前会话的最后一次问答（用户 + 助手两条消息）"""
        with sqlite3.connect(self.db_path) as conn:
            # 获取最后两条消息的 id
            rows = conn.execute(
                "SELECT id, role FROM messages WHERE session_id = ? ORDER BY id DESC LIMIT 2",
                (session_id,),
            ).fetchall()
            ids = [r[0] for r in rows]
            if ids:
                conn.execute(
                    f"DELETE FROM messages WHERE id IN ({','.join('?' * len(ids))})",
                    ids,
                )
                conn.execute(
                    "UPDATE sessions SET updated_at = ? WHERE id = ?",
                    (datetime.now().isoformat(), session_id),
                )
            conn.commit()


class DirectDB:
    def __init__(self):
        self.db = DatabaseManager(DATABASE_URL)

    def get_schema_markdown(self) -> str:
        tables = self.db.get_tables()
        result_parts = []
        for table_name in tables:
            info = self.db.get_table_info(table_name)
            cols_md = "| 字段名 | 类型 | 可为空 | 主键 | 注释 |\n| --- | --- | --- | --- | --- |\n"
            for col in info["columns"]:
                cols_md += f"| {col['name']} | {col['type']} | {'是' if col['nullable'] else '否'} | {'是' if col['primary_key'] else '否'} | {col['comment']} |\n"
            result_parts.append(f"### {table_name}\n\n{cols_md}")
        return "\n\n".join(result_parts)

    def execute_sql_markdown(self, sql: str) -> str:
        sql_lower = sql.strip().lower()
        if not sql_lower.startswith("select"):
            return "只允许执行 SELECT 查询语句"
        try:
            rows = self.db.execute_raw_sql(sql)
            if not rows:
                return "无数据"
            headers = list(rows[0].keys())
            header_line = "| " + " | ".join(headers) + " |"
            sep_line = "| " + " | ".join(["---"] * len(headers)) + " |"
            data_lines = []
            for row in rows:
                values = []
                for h in headers:
                    v = row.get(h, "")
                    if v is None:
                        v = "NULL"
                    values.append(str(v))
                data_lines.append("| " + " | ".join(values) + " |")
            markdown = f"## SQL 查询结果 ({len(rows)} 条记录)\n\n"
            markdown += header_line + "\n" + sep_line + "\n" + "\n".join(data_lines)
            return markdown
        except Exception as e:
            return f"查询出错: {str(e)}"


def get_llm() -> Optional[BaseChatModel]:
    api_key = os.environ.get("DEEPSEEK_API_KEY") or os.environ.get("DASHSCOPE_API_KEY")
    if not api_key:
        return None
    return ChatOpenAI(
        model="deepseek-chat",
        temperature=0.3,
        api_key=api_key,
        base_url=LLM_API_BASE,
    )


def format_history_for_prompt(messages: list[dict]) -> str:
    lines = []
    for msg in messages:
        role = "用户" if msg["role"] == "user" else "助手"
        lines.append(f"{role}: {msg['content']}")
    return "\n".join(lines)


def build_rag_prompt(question: str, db_context: str, history_text: str, user_id: str = "") -> str:
    time_ctx = get_time_context()
    user_ctx = f"当前用户ID: {user_id}" if user_id else "当前用户: 未知"
    return f"""你是一个智能的图书管理系统助手。你的知识来源于数据库中的实时数据，以及和用户的对话历史。

{time_ctx}
{user_ctx}

【对话历史】
{history_text if history_text else "暂无对话历史"}

【数据库查询结果 - 当前数据快照】
{db_context if db_context else "暂无相关数据"}

【用户当前问题】
{question}

请根据上述对话历史和数据库查询结果回答用户的问题。要求：
1. 如果数据库中有相关数据，请基于数据回答，给出具体的数字和细节
2. 如果数据库中没有找到相关数据，如实告知用户
3. 结合对话历史中的上下文，使回答连贯自然
4. 用中文回答，清晰有条理
5. ⚠️ 数据库中的 status/状态码含义如下，回答时**必须**转换：
   - **副本状态** `t_book_copy.status`：**1=在架 2=借出 3=待上架 4=丢失**
   - **借阅状态** `t_borrow_record.status`：1=借阅中 2=已归还 3=超期 4=待审核
   - **用户状态** `t_student.status` / `t_teacher.status`：1=正常 2=超期锁定 3=禁用
   - **图书状态** `t_book.status`：1=上架(已发布) 0=下架，**这个字段不代表副本是否在架**
   - 显示为「在架」「已借出」「待上架」「已丢失」等，不要出现数字编码
   - ❌ 本系统没有「预约」功能，不要把任何状态说成「预约」
   - ⚠️ 判断一本书能不能借，要看 `t_book_copy` 表里有没有 `status = 1`（在架）的副本，**不是**看 `t_book.status`
6. ⚠️ 查询 `t_book_copy` 时必须加 `AND deleted = 0 AND status != 4`。其他表中如果有 `deleted` 字段也要过滤，没有则不加
7. 关于借阅规则：查询 `t_borrow_config` 表获取 `max_books`(最大册数) 和 `max_days`(最大天数)
   - 查询用户当前借阅数：`SELECT COUNT(*) FROM t_borrow_record WHERE user_id = ? AND status IN (1,3,4)`
   - **剩余可借 = max_books - 当前借阅数**，要计算后告诉用户
   - **不要**把 max_books 直接说成「还可借」
8. 关于寒暑假规则：`t_holiday_config` 表中有 `type`(1=暑假 2=寒假)、`start_date`、`end_date`、`extension_days`(延期天数)，查询时不需要加 `deleted` 条件
9. 查询图书位置时，同时查出 `shelf_floor`(楼层)、`shelf_zone`(区域)、`shelf_no`(架号) 字段
10. 回答要自然、实用，像图书馆工作人员一样说话，不要像在展示数据库数据
11. ⚠️ 关于新书借阅：
    - 「新书」的判断标准是 `t_book.create_time`（创建时间/上架时间）在最近 30 天内，**不是用 publish_year（出版年份）来判断**
    - 例如：一本书 publish_year='2021' 但 create_time='2026-07-01'，只要创建时间在30天内就算新书
    - `t_borrow_config.max_new_books` 是最大新书借阅数，查询用：`SELECT max_new_books FROM t_borrow_config WHERE role = ? AND education_level = ?`
    - 统计当前已借新书数用：`SELECT COUNT(*) FROM t_borrow_record r JOIN t_book_copy cp ON r.copy_id = cp.id JOIN t_book b ON cp.book_id = b.id WHERE r.user_id = ? AND r.status IN (1,3,4) AND b.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)`
    - 用户借新书时，既要检查总额度（max_books），也要检查新书额度（max_new_books）
    - 计算剩余可借新书：**max_new_books - 当前已借新书数**
    - 告诉用户「目前借了X本新书，还可借Y本新书」"""

def build_sql_generation_prompt(question: str, schema: str, history_text: str, user_id: str = "") -> str:
    time_ctx = get_time_context()
    user_ctx = f"当前用户ID: {user_id}" if user_id else "当前用户: 未知"
    return f"""你是一个 SQL 专家。根据用户的提问、数据库表结构和对话历史，生成一条 SQL 查询语句。

{time_ctx}
{user_ctx}

【数据库表结构】
{schema}

【对话历史】
{history_text if history_text else "暂无对话历史"}

【用户问题】
{question}

要求：
1. 只输出 SQL 语句本身，不要任何解释
2. 只使用 SELECT 查询
3. 注意表名和字段名必须与数据库表结构完全一致
4. ⚠️ 对包含 `deleted` 字段的表，才加 `AND deleted = 0`；没有 `deleted` 字段的表（如 `t_borrow_config`）不加
5. 查询副本(`t_book_copy`)时，必须加上 `AND deleted = 0 AND status != 4`
6. 查询「在架」图书时，查 `t_book_copy.status = 1` 的副本，**不是**查 `t_book.status`
7. 查询借阅配置时使用 `t_borrow_config` 表，包含 `max_books`(最大册数)、`max_days`(最大天数)、`fine_per_day`(滞纳金)、`max_new_books`(最大新书借阅数)
8. 查询寒暑假规则用 `t_holiday_config` 表（没有 `deleted` 字段，不加条件）
9. 查询用户当前借阅量时：`SELECT COUNT(*) FROM t_borrow_record WHERE user_id = ? AND status IN (1,3,4)`
10. ⚠️ 「新书」的判断依据是 `t_book.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)`，**不是 publish_year**
10. 如果用户问题与数据库无关，直接输出：NO_QUERY_NEEDED

【参考示例 - 遇到类似问题请参考以下SQL的写法】

-- 用户问：有哪些Java相关的书？（按书名/作者搜索）
SELECT id, isbn, title, author, publisher FROM t_book WHERE title LIKE '%Java%' AND deleted = 0;

-- 用户问：《Java核心技术》还有几本可以借？（查询在架副本数）
SELECT COUNT(*) FROM t_book_copy WHERE book_id = 1 AND deleted = 0 AND status = 1;

-- 用户问：《Java核心技术》放在哪里？（查询所有副本及位置）
SELECT cp.id, cp.barcode, cp.shelf_floor, cp.shelf_zone, cp.shelf_no, cp.status FROM t_book_copy cp WHERE cp.book_id = 1 AND cp.deleted = 0 AND cp.status != 4;

-- 用户问：某学生现在借了哪些书？（查询当前借阅列表）
SELECT r.id, b.title, r.borrow_date, r.due_date, r.status FROM t_borrow_record r JOIN t_book_copy cp ON r.copy_id = cp.id JOIN t_book b ON cp.book_id = b.id WHERE r.user_id = 8 AND r.status IN (1,3,4) ORDER BY r.due_date;

-- 用户问：本科生能借几本书？（查询借阅限额）
SELECT max_books, max_days, fine_per_day, max_new_books FROM t_borrow_config WHERE role = 1 AND education_level = 2;

-- 用户问：《Java核心技术》的评价怎么样？（查询图书评论）
SELECT c.rating, c.content, s.name FROM t_comment c LEFT JOIN t_student s ON c.user_id = s.id WHERE c.book_id = 1 ORDER BY c.create_time DESC;

-- 用户问：最热门的书有哪些？（热门图书TOP5）
SELECT b.id, b.title, b.author, COUNT(r.id) AS borrow_count FROM t_book b JOIN t_book_copy cp ON b.id = cp.book_id JOIN t_borrow_record r ON cp.id = r.copy_id WHERE b.deleted = 0 GROUP BY b.id ORDER BY borrow_count DESC LIMIT 5;

-- 用户问：哪些书超期了？（查询超期图书）
SELECT r.id, b.title, COALESCE(s.name, t.name) AS user_name, r.due_date, DATEDIFF(CURDATE(), r.due_date) AS overdue_days FROM t_borrow_record r JOIN t_book_copy cp ON r.copy_id = cp.id JOIN t_book b ON cp.book_id = b.id LEFT JOIN t_student s ON r.user_id = s.id LEFT JOIN t_teacher t ON r.user_id = t.id WHERE r.status = 3 ORDER BY overdue_days DESC;

-- 用户问：图书馆有哪些分类，各有多少本书？（各分类图书数）
SELECT c.name, COUNT(b.id) AS book_count FROM t_category c LEFT JOIN t_book b ON c.id = b.category_id AND b.deleted = 0 WHERE c.deleted = 0 GROUP BY c.id ORDER BY book_count DESC;

-- 用户问：寒暑假借阅规则是什么？（寒暑假延期规则）
SELECT type, start_date, end_date, extension_days FROM t_holiday_config ORDER BY type;

-- 用户问：我还能借新书吗？（查新书限额 + 当前已借新书数）
SELECT (SELECT max_new_books FROM t_borrow_config WHERE role = 1 AND education_level = 2) AS max_new,
       (SELECT COUNT(*) FROM t_borrow_record r JOIN t_book_copy cp ON r.copy_id = cp.id JOIN t_book b ON cp.book_id = b.id WHERE r.user_id = 8 AND r.status IN (1,3,4) AND b.create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)) AS current_new;

"""


class RAGChat:
    def __init__(self):
        self.llm = get_llm()
        self.memory = PersistentMemory()
        self.db = DirectDB()
        self._schema_cache = None

    def is_available(self) -> bool:
        return self.llm is not None

    def _fetch_schema(self) -> str:
        if self._schema_cache:
            return self._schema_cache
        schema = self.db.get_schema_markdown()
        self._schema_cache = schema
        return schema

    def _execute_sql(self, sql: str) -> str:
        return self.db.execute_sql_markdown(sql)

    def _safe_generate_text(self, prompt: str) -> str:
        response = self.llm.generate([[HumanMessage(content=prompt)]])
        gen = response.generations[0][0]
        if hasattr(gen, "text"):
            return gen.text.strip()
        return str(gen).strip()

    def _generate_sql(self, question: str, history: list[dict], user_id: str = "") -> str:
        schema = self._fetch_schema()
        history_text = format_history_for_prompt(history[-6:] if len(history) > 6 else history)
        prompt = build_sql_generation_prompt(question, schema, history_text, user_id)
        sql = self._safe_generate_text(prompt)
        sql = sql.replace("```sql", "").replace("```", "").strip()
        return sql

    def _get_db_context(self, question: str, history: list[dict], user_id: str = "") -> str:
        sql = self._generate_sql(question, history, user_id)
        if "NO_QUERY_NEEDED" in sql:
            return ""
        try:
            result = self._execute_sql(sql)
            return f"执行的SQL: {sql}\n\n查询结果:\n{result}"
        except Exception as e:
            return f"尝试执行的SQL: {sql}\n查询出错: {str(e)}"

    def ask(self, question: str, session_id: str, user_id: str = "") -> str:
        history = self.memory.get_messages(session_id)

        self.memory.add_message(session_id, "user", question)

        db_context = self._get_db_context(question, history, user_id)

        history_text = format_history_for_prompt(history[-10:] if len(history) > 10 else history)
        prompt = build_rag_prompt(question, db_context, history_text, user_id)

        answer = self._safe_generate_text(prompt)

        self.memory.add_message(session_id, "assistant", answer)
        return answer
