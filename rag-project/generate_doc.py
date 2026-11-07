from docx import Document
from docx.shared import Inches, Pt, Cm, RGBColor
from docx.enum.text import WD_PARAGRAPH_ALIGNMENT
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn

doc = Document()

style = doc.styles['Normal']
style.font.name = '宋体'
style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
style.font.size = Pt(11)
style.paragraph_format.line_spacing = 1.5

for s_name in ['Heading 1', 'Heading 2', 'Heading 3', 'Title']:
    if s_name in [s.name for s in doc.styles]:
        hs = doc.styles[s_name]
        hs.font.name = '宋体'
        hs.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')

for section in doc.sections:
    section.top_margin = Cm(2.5)
    section.bottom_margin = Cm(2.5)
    section.left_margin = Cm(2.5)
    section.right_margin = Cm(2.5)

title = doc.add_heading('rag-project 智能问答系统', level=0)
title.alignment = WD_PARAGRAPH_ALIGNMENT.CENTER

p = doc.add_paragraph()
p.alignment = WD_PARAGRAPH_ALIGNMENT.CENTER
run = p.add_run('基于通义千问 qwen3-max · Text-to-SQL · 长期会话记忆')
run.font.size = Pt(12)
run.font.color.rgb = RGBColor(100, 100, 100)

doc.add_paragraph()

doc.add_heading('一、项目概述', level=1)
doc.add_paragraph(
    'rag-project 是一个面向教室管理场景的智能问答系统。用户可以用自然语言提问，'
    '系统自动理解意图、生成 SQL 查询数据库、并返回自然语言回答。'
    '后端集成了大语言模型（LLM）、数据库操作和会话记忆管理，'
    '前端基于 Streamlit 提供即时交互界面。'
)

doc.add_heading('二、技术栈', level=1)

table = doc.add_table(rows=8, cols=2, style='Light Grid Accent 1')
table.alignment = WD_TABLE_ALIGNMENT.CENTER

technologies = [
    ('层级', '技术选型'),
    ('LLM', '通义千问 qwen3-max（DashScope 兼容接口）'),
    ('LLM 框架', 'LangChain（ChatOpenAI 代理调用）'),
    ('数据库 ORM', 'SQLAlchemy + PyMySQL'),
    ('业务数据库', 'MySQL（classroom_management）'),
    ('会话记忆', 'SQLite（chat_history.db）'),
    ('前端框架', 'Streamlit'),
    ('配置管理', 'python-dotenv + .env 文件'),
]

for i, (key, value) in enumerate(technologies):
    table.rows[i].cells[0].text = key
    table.rows[i].cells[1].text = value

doc.add_paragraph()

doc.add_heading('三、项目结构', level=1)

code = doc.add_paragraph()
run = code.add_run(
    'rag-project/\n'
    '├── app.py                  # 前端入口（Streamlit）\n'
    '├── generate_doc.py         # 文档生成脚本\n'
    '├── requirements.txt        # 依赖清单\n'
    '├── .env                    # 环境变量（数据库 + API Key）\n'
    '├── chat_history.db         # 会话持久化 SQLite\n'
    '├── rag_core/\n'
    '│   └── rag_chain.py        # 后端核心（RAGChat 主流程）\n'
    '└── mcp_server/\n'
    '    ├── config.py           # 数据库连接配置\n'
    '    └── server.py           # DatabaseManager（SQLAlchemy 操作层）'
)
run.font.name = '宋体'
run.font.size = Pt(9.5)

doc.add_heading('四、后端实现（rag_core/rag_chain.py）', level=1)

doc.add_heading('4.1 核心模块', level=2)

modules = [
    ('PersistentMemory', '基于 SQLite 的会话记忆管理。支持创建/删除会话、增删消息记录，'
     '所有对话历史持久化在 chat_history.db 中。'),
    ('DirectDB', '直接封装 DatabaseManager（SQLAlchemy），绕过 MCP 协议，'
     '提供 get_schema_markdown() 获取表结构，execute_sql_markdown() 执行 SQL 并格式化为 Markdown。'),
    ('RAGChat', '核心调度类，统筹 LLM 调用、数据库查询和会话记忆。'
     '内部提供一个 SafeGenerateText 方法，统一处理 LLM 返回格式兼容问题。'),
]

for name, desc in modules:
    p = doc.add_paragraph()
    run = p.add_run(f'{name}：')
    run.bold = True
    p.add_run(desc)

doc.add_heading('4.2 核心流程（ask 方法）', level=2)

steps = [
    ('Step 1', '获取会话历史', '从 SQLite 加载当前会话的最近消息'),
    ('Step 2', '记录用户提问', '将用户问题写入会话记忆'),
    ('Step 3', '生成 SQL', 'LLM 根据表结构 + 历史 + 问题生成 SELECT 查询语句'),
    ('Step 4', '执行 SQL', 'DirectDB 执行 SQL 并从 MySQL 取回结果'),
    ('Step 5', '组织回答', 'LLM 根据 SQL 结果 + 历史 + 问题生成自然语言回答'),
    ('Step 6', '记录回答', '将助手回答写入会话记忆，返回给前端'),
]

table2 = doc.add_table(rows=len(steps) + 1, cols=3, style='Light Grid Accent 1')
table2.alignment = WD_TABLE_ALIGNMENT.CENTER
table2.rows[0].cells[0].text = '步骤'
table2.rows[0].cells[1].text = '操作'
table2.rows[0].cells[2].text = '说明'

for i, (step, action, desc) in enumerate(steps):
    table2.rows[i + 1].cells[0].text = step
    table2.rows[i + 1].cells[1].text = action
    table2.rows[i + 1].cells[2].text = desc

doc.add_paragraph()
flow_p = doc.add_paragraph()
run = flow_p.add_run('流程图：用户提问 → LLM 生成 SQL → 查询 MySQL → LLM 组织最终回答 → 展示给用户')
run.bold = True
run.font.color.rgb = RGBColor(0, 80, 160)

doc.add_paragraph()

doc.add_heading('4.3 两个 Prompt 模板', level=2)

p = doc.add_paragraph()
run = p.add_run('build_sql_generation_prompt：')
run.bold = True
p.add_run('将数据库表结构（字段名、类型、注释）、对话历史、用户问题输入 LLM，要求其输出纯 SQL 语句。')

p = doc.add_paragraph()
run = p.add_run('build_rag_prompt：')
run.bold = True
p.add_run('将 SQL 查询结果（表格形式）、对话历史、用户问题输入 LLM，要求其基于数据生成自然语言回答。')

doc.add_heading('五、前端实现（app.py）', level=1)

doc.add_paragraph('基于 Streamlit 构建的单页 Web 应用，主要功能包括：')

features = [
    ('侧边栏会话管理', '左侧展示对话列表，支持新建对话、删除对话、切换会话'),
    ('聊天界面', '使用 st.chat_message + st.chat_input 构建，一问一答即时展示'),
    ('状态保持', '通过 st.session_state 持久化 RAGChat 实例和当前会话 ID'),
    ('加载提示', '调用 LLM 和数据库时显示 spinner，提升交互体验'),
    ('错误处理', '捕获异常并在界面展示错误信息，同时记录到会话历史'),
]

for title, desc in features:
    p = doc.add_paragraph()
    run = p.add_run(f'{title}：')
    run.bold = True
    p.add_run(desc)

doc.add_heading('六、数据流总览', level=1)

flow_text = (
    '[用户] 输入问题（如"总共有多少间教室？"）\n'
    '   ↓\n'
    '[Streamlit 前端] app.py → 调用 RAGChat.ask()\n'
    '   ↓\n'
    '[RAGChat 后端] rag_chain.py\n'
    '   ├─1. 从 SQLite 加载历史对话\n'
    '   ├─2. 将用户问题存入 SQLite\n'
    '   ├─3. LLM 根据表结构生成 SQL\n'
    '   ├─4. DirectDB 执行 SQL → MySQL 返回结果\n'
    '   └─5. LLM 根据结果 + 历史生成最终回答\n'
    '   ↓\n'
    '[Streamlit 前端] 展示回答 → st.rerun() 刷新'
)

p = doc.add_paragraph()
run = p.add_run(flow_text)
run.font.name = '宋体'
run.font.size = Pt(9.5)

doc.add_paragraph()

footer = doc.add_paragraph()
footer.alignment = WD_PARAGRAPH_ALIGNMENT.CENTER
run = footer.add_run('—— 文档结束 ——')
run.font.color.rgb = RGBColor(150, 150, 150)
run.font.size = Pt(10)

doc.save('rag-project_intro_v2.docx')
print('文档已生成: rag-project_intro_v2.docx')
