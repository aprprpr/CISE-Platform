import pandas as pd
from sqlalchemy import create_engine
import os
import sys

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '5XHdmysqlmm',
    'database': 'study',
    'charset': 'utf8mb4'
}

def get_connection():
    connection_string = (
        f"mysql+pymysql://{DB_CONFIG['user']}:{DB_CONFIG['password']}"
        f"@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}"
        f"?charset={DB_CONFIG['charset']}"
    )
    return create_engine(connection_string, echo=False)

def export_interaction_data(engine):
    print("正在导出用户-项目交互数据...")

    query = """
        SELECT
            s.student_id as user_id,
            a.project_id as item_id,
            1 as interaction_type,
            a.pass_status,
            a.projectContent,
            a.projectName
        FROM apply a
        JOIN student s ON a.student_id = s.student_id
        WHERE a.pass_status = '通过'
        ORDER BY s.student_id, a.project_id
    """

    df = pd.read_sql(query, engine)
    df.to_csv('data/interactions.csv', index=False, encoding='utf-8-sig')
    print(f"  -> 导出完成: {len(df)} 条记录")
    print(f"  -> 用户数: {df['user_id'].nunique()}")
    print(f"  -> 项目数: {df['item_id'].nunique()}")
    return df

def export_project_data(engine):
    print("\n正在导出项目信息...")

    query = """
        SELECT
            p.project_id,
            p.projectName as project_name,
            p.projectInfo as project_info,
            p.teacherId as teacher_id,
            t.teacherName as teacher_name
        FROM project p
        LEFT JOIN teacher t ON p.teacherId = t.teacher_id
        WHERE p.project_id IS NOT NULL
        ORDER BY p.project_id
    """

    df = pd.read_sql(query, engine)
    df.to_csv('data/projects.csv', index=False, encoding='utf-8-sig')
    print(f"  -> 导出完成: {len(df)} 条记录")
    return df

def export_evaluation_scores(engine):
    print("\n正在导出评分数据...")

    query = """
        SELECT
            user_id,
            project_id,
            score,
            score_type
        FROM evaluation_score
        ORDER BY project_id, user_id
    """

    df = pd.read_sql(query, engine)
    df.to_csv('data/evaluation_scores.csv', index=False, encoding='utf-8-sig')
    print(f"  -> 导出完成: {len(df)} 条记录")
    return df

def export_contribution_data(engine):
    print("\n正在导出成员贡献数据...")

    query = """
        SELECT
            project_id,
            user_id,
            contribution_rate
        FROM project_member_contribution
        ORDER BY project_id, user_id
    """

    df = pd.read_sql(query, engine)
    df.to_csv('data/contributions.csv', index=False, encoding='utf-8-sig')
    print(f"  -> 导出完成: {len(df)} 条记录")
    return df

def export_student_data(engine):
    print("\n正在导出学生信息...")

    query = """
        SELECT
            student_id as user_id,
            studentName as user_name,
            studentSex as user_sex,
            studentEmail as user_email
        FROM student
        ORDER BY student_id
    """

    df = pd.read_sql(query, engine)
    df.to_csv('data/students.csv', index=False, encoding='utf-8-sig')
    print(f"  -> 导出完成: {len(df)} 条记录")
    return df

def export_all_data():
    print("=" * 60)
    print("开始数据导出...")
    print("=" * 60)

    os.makedirs('data', exist_ok=True)

    try:
        engine = get_connection()
        print(f"数据库连接成功: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}\n")

        export_interaction_data(engine)
        export_project_data(engine)
        export_evaluation_scores(engine)
        export_contribution_data(engine)
        export_student_data(engine)

        print("\n" + "=" * 60)
        print("数据导出完成！")
        print("=" * 60)
        print(f"数据保存位置: {os.path.abspath('data')}")

        engine.dispose()

    except Exception as e:
        print(f"\n错误: {e}")
        raise

if __name__ == '__main__':
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    export_all_data()
