import json
import sys
import os
from typing import Any, Optional

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy import (
    create_engine, inspect, text, Table, MetaData,
    select, func, and_
)
from sqlalchemy.engine import Engine
from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import (
    TextContent,
    Tool,
    CallToolResult,
)

from config import DATABASE_URL


class DatabaseManager:
    def __init__(self, database_url: str):
        self.engine: Engine = create_engine(database_url, pool_pre_ping=True)
        self.metadata = MetaData()

    def get_tables(self) -> list[str]:
        inspector = inspect(self.engine)
        return inspector.get_table_names()

    def get_table_info(self, table_name: str) -> dict[str, Any]:
        inspector = inspect(self.engine)
        columns = inspector.get_columns(table_name)
        return {
            "name": table_name,
            "columns": [
                {
                    "name": col["name"],
                    "type": str(col["type"]),
                    "nullable": col.get("nullable", True),
                    "primary_key": col.get("primary_key", False),
                    "comment": col.get("comment", ""),
                }
                for col in columns
            ],
        }

    def query(self, table_name: str, conditions: Optional[dict[str, Any]] = None,
              limit: int = 100, offset: int = 0, order_by: Optional[str] = None,
              columns: Optional[list[str]] = None) -> list[dict[str, Any]]:
        table = Table(table_name, self.metadata, autoload_with=self.engine)
        
        if columns:
            col_objects = [table.c[col] for col in columns if col in table.c]
            stmt = select(*col_objects)
        else:
            stmt = select(table)

        if conditions:
            filters = []
            for key, value in conditions.items():
                if key in table.c:
                    filters.append(table.c[key] == value)
            if filters:
                stmt = stmt.where(and_(*filters))

        if order_by and order_by in table.c:
            stmt = stmt.order_by(table.c[order_by])

        stmt = stmt.offset(offset).limit(limit)

        with self.engine.connect() as conn:
            result = conn.execute(stmt)
            rows = result.fetchall()
            return [dict(row._mapping) for row in rows]

    def insert(self, table_name: str, data: dict[str, Any]) -> dict[str, Any]:
        table = Table(table_name, self.metadata, autoload_with=self.engine)
        stmt = table.insert().values(**data)
        with self.engine.connect() as conn:
            result = conn.execute(stmt)
            conn.commit()
            return {"inserted_id": result.inserted_primary_key[0], "row_count": result.rowcount}

    def update(self, table_name: str, data: dict[str, Any],
               conditions: dict[str, Any]) -> dict[str, Any]:
        table = Table(table_name, self.metadata, autoload_with=self.engine)
        filters = [table.c[key] == value for key, value in conditions.items()]
        stmt = table.update().where(and_(*filters)).values(**data)
        with self.engine.connect() as conn:
            result = conn.execute(stmt)
            conn.commit()
            return {"row_count": result.rowcount}

    def delete(self, table_name: str, conditions: dict[str, Any]) -> dict[str, Any]:
        table = Table(table_name, self.metadata, autoload_with=self.engine)
        filters = [table.c[key] == value for key, value in conditions.items()]
        stmt = table.delete().where(and_(*filters))
        with self.engine.connect() as conn:
            result = conn.execute(stmt)
            conn.commit()
            return {"row_count": result.rowcount}

    def aggregate(self, table_name: str, group_by: str,
                  aggregate_func: str = "COUNT",
                  aggregate_column: str = "id") -> list[dict[str, Any]]:
        table = Table(table_name, self.metadata, autoload_with=self.engine)
        agg_func_map = {
            "COUNT": func.count,
            "SUM": func.sum,
            "AVG": func.avg,
            "MAX": func.max,
            "MIN": func.min,
        }
        agg_func = agg_func_map.get(aggregate_func.upper(), func.count)
        group_col = table.c[group_by]
        agg_col = table.c[aggregate_column]
        stmt = select(
            group_col,
            agg_func(agg_col).label(f"{aggregate_func.lower()}_{aggregate_column}")
        ).group_by(group_col).order_by(group_col)

        with self.engine.connect() as conn:
            result = conn.execute(stmt)
            rows = result.fetchall()
            return [dict(row._mapping) for row in rows]

    def execute_raw_sql(self, sql: str) -> list[dict[str, Any]]:
        with self.engine.connect() as conn:
            result = conn.execute(text(sql))
            if result.returns_rows:
                rows = result.fetchall()
                return [dict(row._mapping) for row in rows]
            conn.commit()
            return [{"affected_rows": result.rowcount}]


db_manager = DatabaseManager(DATABASE_URL)
server = Server("classroom-mcp")


def _table_to_markdown(rows: list[dict[str, Any]]) -> str:
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
    return header_line + "\n" + sep_line + "\n" + "\n".join(data_lines)


@server.list_tools()
async def list_tools() -> list[Tool]:
    return [
        Tool(
            name="list_tables",
            description="获取数据库中所有可用表的列表",
            inputSchema={"type": "object", "properties": {}},
        ),
        Tool(
            name="get_table_info",
            description="获取指定表的字段结构信息",
            inputSchema={
                "type": "object",
                "properties": {
                    "table_name": {
                        "type": "string",
                        "description": "表名",
                    }
                },
                "required": ["table_name"],
            },
        ),
        Tool(
            name="query_table",
            description="查询指定表中的数据，支持条件过滤、分页和排序",
            inputSchema={
                "type": "object",
                "properties": {
                    "table_name": {"type": "string", "description": "表名"},
                    "conditions": {
                        "type": "object",
                        "description": "过滤条件，如 {\"status\": 1}",
                    },
                    "limit": {"type": "integer", "description": "返回记录数限制", "default": 100},
                    "offset": {"type": "integer", "description": "偏移量", "default": 0},
                    "order_by": {"type": "string", "description": "排序字段"},
                    "columns": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "要查询的列名列表",
                    },
                },
                "required": ["table_name"],
            },
        ),
        Tool(
            name="insert_record",
            description="向指定表中插入一条新记录",
            inputSchema={
                "type": "object",
                "properties": {
                    "table_name": {"type": "string", "description": "表名"},
                    "data": {
                        "type": "object",
                        "description": "要插入的数据，如 {\"name\": \"张三\", \"age\": 20}",
                    },
                },
                "required": ["table_name", "data"],
            },
        ),
        Tool(
            name="update_record",
            description="更新指定表中符合条件的记录",
            inputSchema={
                "type": "object",
                "properties": {
                    "table_name": {"type": "string", "description": "表名"},
                    "data": {"type": "object", "description": "要更新的数据"},
                    "conditions": {
                        "type": "object",
                        "description": "更新条件，如 {\"id\": 1}",
                    },
                },
                "required": ["table_name", "data", "conditions"],
            },
        ),
        Tool(
            name="delete_record",
            description="删除指定表中符合条件的记录（逻辑删除）",
            inputSchema={
                "type": "object",
                "properties": {
                    "table_name": {"type": "string", "description": "表名"},
                    "conditions": {"type": "object", "description": "删除条件"},
                },
                "required": ["table_name", "conditions"],
            },
        ),
        Tool(
            name="aggregate_data",
            description="对指定表进行分组聚合统计",
            inputSchema={
                "type": "object",
                "properties": {
                    "table_name": {"type": "string", "description": "表名"},
                    "group_by": {"type": "string", "description": "分组字段名"},
                    "aggregate_func": {
                        "type": "string",
                        "description": "聚合函数: COUNT, SUM, AVG, MAX, MIN",
                        "default": "COUNT",
                    },
                    "aggregate_column": {
                        "type": "string",
                        "description": "要聚合的列名",
                        "default": "id",
                    },
                },
                "required": ["table_name", "group_by"],
            },
        ),
        Tool(
            name="execute_sql",
            description="执行自定义SQL查询语句（只读查询）",
            inputSchema={
                "type": "object",
                "properties": {
                    "sql": {"type": "string", "description": "SQL查询语句"},
                },
                "required": ["sql"],
            },
        ),
        Tool(
            name="get_table_schema",
            description="获取所有表的完整结构信息，包括字段名、类型、注释等",
            inputSchema={"type": "object", "properties": {}},
        ),
    ]


@server.call_tool()
async def call_tool(name: str, arguments: dict) -> CallToolResult:
    try:
        if name == "list_tables":
            tables = db_manager.get_tables()
            markdown = "## 数据库表列表\n\n" + "\n".join(f"- {t}" for t in tables)
            return CallToolResult(content=[TextContent(type="text", text=markdown)])

        elif name == "get_table_schema":
            tables = db_manager.get_tables()
            result_parts = []
            for table_name in tables:
                info = db_manager.get_table_info(table_name)
                cols_md = "| 字段名 | 类型 | 可为空 | 主键 | 注释 |\n| --- | --- | --- | --- | --- |\n"
                for col in info["columns"]:
                    cols_md += f"| {col['name']} | {col['type']} | {'是' if col['nullable'] else '否'} | {'是' if col['primary_key'] else '否'} | {col['comment']} |\n"
                result_parts.append(f"### {table_name}\n\n{cols_md}")
            return CallToolResult(content=[TextContent(type="text", text="\n\n".join(result_parts))])

        elif name == "get_table_info":
            info = db_manager.get_table_info(arguments["table_name"])
            cols_md = "| 字段名 | 类型 | 可为空 | 主键 | 注释 |\n| --- | --- | --- | --- | --- |\n"
            for col in info["columns"]:
                cols_md += f"| {col['name']} | {col['type']} | {'是' if col['nullable'] else '否'} | {'是' if col['primary_key'] else '否'} | {col['comment']} |\n"
            return CallToolResult(content=[TextContent(type="text", text=cols_md)])

        elif name == "query_table":
            rows = db_manager.query(
                table_name=arguments["table_name"],
                conditions=arguments.get("conditions"),
                limit=arguments.get("limit", 100),
                offset=arguments.get("offset", 0),
                order_by=arguments.get("order_by"),
                columns=arguments.get("columns"),
            )
            markdown = f"## 查询结果 ({len(rows)} 条记录)\n\n{_table_to_markdown(rows)}"
            return CallToolResult(content=[TextContent(type="text", text=markdown)])

        elif name == "insert_record":
            result = db_manager.insert(arguments["table_name"], arguments["data"])
            return CallToolResult(
                content=[TextContent(type="text", text=f"插入成功！插入ID: {result['inserted_id']}")]
            )

        elif name == "update_record":
            result = db_manager.update(arguments["table_name"], arguments["data"], arguments["conditions"])
            return CallToolResult(
                content=[TextContent(type="text", text=f"更新成功！影响行数: {result['row_count']}")]
            )

        elif name == "delete_record":
            result = db_manager.delete(arguments["table_name"], arguments["conditions"])
            return CallToolResult(
                content=[TextContent(type="text", text=f"删除成功！影响行数: {result['row_count']}")]
            )

        elif name == "aggregate_data":
            rows = db_manager.aggregate(
                table_name=arguments["table_name"],
                group_by=arguments["group_by"],
                aggregate_func=arguments.get("aggregate_func", "COUNT"),
                aggregate_column=arguments.get("aggregate_column", "id"),
            )
            markdown = f"## 聚合结果\n\n{_table_to_markdown(rows)}"
            return CallToolResult(content=[TextContent(type="text", text=markdown)])

        elif name == "execute_sql":
            sql = arguments["sql"].strip().lower()
            if not sql.startswith("select"):
                return CallToolResult(
                    content=[TextContent(type="text", text="只允许执行 SELECT 查询语句")]
                )
            rows = db_manager.execute_raw_sql(arguments["sql"])
            markdown = f"## SQL 查询结果 ({len(rows)} 条记录)\n\n{_table_to_markdown(rows)}"
            return CallToolResult(content=[TextContent(type="text", text=markdown)])

        else:
            return CallToolResult(
                content=[TextContent(type="text", text=f"未知工具: {name}")]
            )
    except Exception as e:
        return CallToolResult(
            content=[TextContent(type="text", text=f"执行出错: {str(e)}")]
        )


async def run_server():
    async with stdio_server() as (read_stream, write_stream):
        await server.run(read_stream, write_stream, server.create_initialization_options())


if __name__ == "__main__":
    import asyncio
    asyncio.run(run_server())
