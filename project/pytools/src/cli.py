#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
PyTools命令行接口
供Java后端通过命令行调用生成图表
"""

import argparse
import json
import sys
import os
import traceback
from pathlib import Path

# 设置UTF-8编码
import locale
if sys.platform.startswith('win'):
    # Windows 平台设置编码
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')

# 添加当前目录到Python路径
sys.path.insert(0, os.path.dirname(__file__))

from charts import MatplotlibCharts, PlotlyCharts, SeabornCharts


class ChartCLI:
    """图表命令行接口"""
    
    def __init__(self):
        self.generators = {
            'matplotlib': MatplotlibCharts,
            'plotly': PlotlyCharts,
            'seaborn': SeabornCharts
        }
    
    def create_chart(self, engine, chart_type, config_file, output_dir=None):
        """
        创建图表
        
        Args:
            engine: 图表引擎 (matplotlib/plotly/seaborn)
            chart_type: 图表类型 (line/bar/pie/scatter/heatmap)
            config_file: JSON配置文件路径
            output_dir: 输出目录
            
        Returns:
            dict: 包含成功状态和输出路径的结果
        """
        try:
            # 读取配置文件
            with open(config_file, 'r', encoding='utf-8') as f:
                config = json.load(f)
            
            # 获取参数
            data = config.get('data', {})
            chart_config = config.get('chart_config', {})
            
            if not data:
                return {'success': False, 'error': '数据不能为空'}
            
            # 创建图表生成器
            if engine not in self.generators:
                return {'success': False, 'error': f'不支持的引擎: {engine}'}
            
            generator_class = self.generators[engine]
            if output_dir:
                chart_gen = generator_class(output_dir=output_dir)
            else:
                chart_gen = generator_class()
            
            # 根据图表类型调用相应方法
            method_map = {
                'line': 'create_line_chart',
                'bar': 'create_bar_chart', 
                'pie': 'create_pie_chart',
                'scatter': 'create_scatter_plot',
                'heatmap': 'create_heatmap'
            }
            
            if chart_type not in method_map:
                return {'success': False, 'error': f'不支持的图表类型: {chart_type}'}
            
            method_name = method_map[chart_type]
            method = getattr(chart_gen, method_name)
            
            # 调用方法生成图表 - 根据不同图表类型传递正确参数
            if chart_type == 'line':
                x_col = chart_config.get('x_column')
                y_cols = chart_config.get('y_columns')
                if x_col is None or y_cols is None:
                    return {'success': False, 'error': 'Line chart requires x_column and y_columns parameters'}
                other_params = {k: v for k, v in chart_config.items() if k not in ['x_column', 'y_columns']}
                output_path = method(data, x_col, y_cols, **other_params)
                
            elif chart_type == 'bar':
                x_col = chart_config.get('x_column')
                if 'y_columns' in chart_config:
                    # 分组柱状图
                    y_cols = chart_config.get('y_columns')
                    if x_col is None or y_cols is None:
                        return {'success': False, 'error': 'Grouped bar chart requires x_column and y_columns parameters'}
                    other_params = {k: v for k, v in chart_config.items() if k not in ['x_column', 'y_columns']}
                    output_path = method(data, x_col, y_cols, **other_params)
                else:
                    # 简单柱状图
                    y_col = chart_config.get('y_column')
                    if x_col is None or y_col is None:
                        return {'success': False, 'error': 'Bar chart requires x_column and y_column parameters'}
                    other_params = {k: v for k, v in chart_config.items() if k not in ['x_column', 'y_column']}
                    output_path = method(data, x_col, y_col, **other_params)
                    
            elif chart_type == 'pie':
                labels_col = chart_config.get('labels_column')
                values_col = chart_config.get('values_column')
                if labels_col is None or values_col is None:
                    return {'success': False, 'error': 'Pie chart requires labels_column and values_column parameters'}
                other_params = {k: v for k, v in chart_config.items() if k not in ['labels_column', 'values_column']}
                output_path = method(data, labels_col, values_col, **other_params)
                
            elif chart_type == 'scatter':
                x_col = chart_config.get('x_column')
                y_col = chart_config.get('y_column')
                if x_col is None or y_col is None:
                    return {'success': False, 'error': 'Scatter plot requires x_column and y_column parameters'}
                other_params = {k: v for k, v in chart_config.items() if k not in ['x_column', 'y_column']}
                output_path = method(data, x_col, y_col, **other_params)
                
            elif chart_type == 'heatmap':
                output_path = method(data, **chart_config)
            else:
                # 默认方式
                output_path = method(data, **chart_config)
            
            return {
                'success': True,
                'output_path': str(output_path),
                'chart_type': chart_type,
                'engine': engine
            }
            
        except FileNotFoundError:
            return {'success': False, 'error': f'配置文件未找到: {config_file}'}
        except json.JSONDecodeError as e:
            return {'success': False, 'error': f'JSON解析错误: {str(e)}'}
        except Exception as e:
            return {'success': False, 'error': f'生成图表时出错: {str(e)}', 'traceback': traceback.format_exc()}
    
    def validate_config(self, config_file):
        """验证配置文件格式"""
        try:
            with open(config_file, 'r', encoding='utf-8') as f:
                config = json.load(f)
            
            required_keys = ['data', 'chart_config']
            missing_keys = [key for key in required_keys if key not in config]
            
            if missing_keys:
                return {'valid': False, 'error': f'缺少必需字段: {missing_keys}'}
            
            # 验证数据格式
            data = config['data']
            if not isinstance(data, dict) or not data:
                return {'valid': False, 'error': '数据必须是非空字典'}
            
            return {'valid': True, 'message': '配置文件格式正确'}
            
        except Exception as e:
            return {'valid': False, 'error': str(e)}


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='PyTools图表生成命令行工具')
    
    # 子命令
    subparsers = parser.add_subparsers(dest='command', help='可用命令')
    
    # 生成图表命令
    create_parser = subparsers.add_parser('create', help='生成图表')
    create_parser.add_argument('--engine', '-e', 
                              choices=['matplotlib', 'plotly', 'seaborn'],
                              required=True, help='图表引擎')
    create_parser.add_argument('--type', '-t',
                              choices=['line', 'bar', 'pie', 'scatter', 'heatmap'],
                              required=True, help='图表类型')
    create_parser.add_argument('--config', '-c', required=True, help='JSON配置文件路径')
    create_parser.add_argument('--output', '-o', help='输出目录')
    create_parser.add_argument('--format', choices=['json', 'simple'], 
                              default='json', help='输出格式')
    
    # 验证配置命令
    validate_parser = subparsers.add_parser('validate', help='验证配置文件')
    validate_parser.add_argument('--config', '-c', required=True, help='JSON配置文件路径')
    
    # 帮助命令
    help_parser = subparsers.add_parser('help', help='显示帮助信息')
    help_parser.add_argument('--topic', choices=['config', 'examples'], help='帮助主题')
    
    args = parser.parse_args()
    
    # 处理命令
    cli = ChartCLI()
    
    if args.command == 'create':
        result = cli.create_chart(
            engine=args.engine,
            chart_type=args.type,
            config_file=args.config,
            output_dir=args.output
        )
        
        if args.format == 'json':
            # 确保只输出JSON，不输出其他信息
            print(json.dumps(result, ensure_ascii=False), flush=True)
        else:
            if result['success']:
                print(result['output_path'])
            else:
                print(f"ERROR: {result['error']}", file=sys.stderr)
                sys.exit(1)
    
    elif args.command == 'validate':
        result = cli.validate_config(args.config)
        print(json.dumps(result, ensure_ascii=False, indent=2))
        if not result['valid']:
            sys.exit(1)
    
    elif args.command == 'help':
        if args.topic == 'config':
            print_config_help()
        elif args.topic == 'examples':
            print_examples_help()
        else:
            print_general_help()
    
    else:
        parser.print_help()


def print_config_help():
    """打印配置文件帮助"""
    help_text = """
配置文件格式说明 (JSON):

{
  "data": {
    "x_column_name": [1, 2, 3, 4, 5],
    "y_column_name": [10, 20, 15, 25, 30],
    "category_column": ["A", "B", "C", "D", "E"]
  },
  "chart_config": {
    "x_column": "x_column_name",
    "y_columns": "y_column_name",  // 或 ["col1", "col2"] 多列
    "title": "图表标题",
    "xlabel": "X轴标签",
    "ylabel": "Y轴标签",
    "filename": "custom_name.png",  // 可选，自动生成
    "figsize": [12, 8],            // matplotlib专用
    "width": 1200,                 // plotly专用
    "height": 600,                 // plotly专用
    "color": "blue",
    "marker": "o",
    "linewidth": 2
  }
}

必需字段:
- data: 图表数据，字典格式
- chart_config: 图表配置参数

数据格式支持:
- 列表: [1, 2, 3, 4, 5]
- 字符串列表: ["A", "B", "C"]
- 数值列表: [1.5, 2.3, 3.7]
"""
    print(help_text)


def print_examples_help():
    """打印使用示例帮助"""
    examples = """
使用示例:

1. 生成折线图:
python cli.py create --engine matplotlib --type line --config line_config.json

2. 生成柱状图并指定输出目录:
python cli.py create -e seaborn -t bar -c bar_config.json -o ./output

3. 验证配置文件:
python cli.py validate --config my_config.json

4. 生成图表并只输出文件路径:
python cli.py create -e plotly -t scatter -c config.json --format simple

配置文件示例:

line_config.json:
{
  "data": {
    "month": ["Jan", "Feb", "Mar", "Apr", "May"],
    "sales": [100, 120, 140, 110, 160],
    "profit": [20, 25, 30, 22, 35]
  },
  "chart_config": {
    "x_column": "month",
    "y_columns": ["sales", "profit"],
    "title": "月度销售趋势",
    "xlabel": "月份",
    "ylabel": "金额"
  }
}

bar_config.json:
{
  "data": {
    "category": ["产品A", "产品B", "产品C", "产品D"],
    "values": [23, 45, 56, 78]
  },
  "chart_config": {
    "x_column": "category",
    "y_column": "values",
    "title": "产品销售对比",
    "color": "skyblue"
  }
}
"""
    print(examples)


def print_general_help():
    """打印通用帮助"""
    help_text = """
PyTools 图表生成工具

用法:
  python cli.py <command> [options]

命令:
  create      生成图表
  validate    验证配置文件
  help        显示帮助信息

图表引擎:
  matplotlib  静态高质量图表，支持多种格式输出
  plotly      交互式Web图表，支持HTML和PNG输出
  seaborn     统计图表，基于matplotlib增强

图表类型:
  line        折线图
  bar         柱状图
  pie         饼图
  scatter     散点图
  heatmap     热力图

更多帮助:
  python cli.py help --topic config     配置文件格式
  python cli.py help --topic examples   使用示例

退出代码:
  0    成功
  1    失败
"""
    print(help_text)


if __name__ == '__main__':
    main()