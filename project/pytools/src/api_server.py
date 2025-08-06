#!/usr/bin/env python3
"""
PyTools Web API服务
提供REST API接口供Java后端调用
"""

import os
import sys
import json
import tempfile
import uuid
from datetime import datetime
from pathlib import Path
from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
import traceback

# 添加当前目录到Python路径
sys.path.insert(0, os.path.dirname(__file__))

from charts import MatplotlibCharts, PlotlyCharts, SeabornCharts

app = Flask(__name__)
CORS(app)  # 允许跨域请求

# 配置
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max file size
app.config['UPLOAD_FOLDER'] = tempfile.gettempdir()
app.config['OUTPUT_FOLDER'] = os.path.join(tempfile.gettempdir(), 'pytools_output')

# 确保输出目录存在
os.makedirs(app.config['OUTPUT_FOLDER'], exist_ok=True)

class ChartAPI:
    """图表API服务类"""
    
    def __init__(self):
        self.generators = {
            'matplotlib': MatplotlibCharts,
            'plotly': PlotlyCharts,
            'seaborn': SeabornCharts
        }
        self.chart_methods = {
            'line': 'create_line_chart',
            'bar': 'create_bar_chart',
            'pie': 'create_pie_chart', 
            'scatter': 'create_scatter_plot',
            'heatmap': 'create_heatmap'
        }
    
    def validate_request(self, data):
        """验证请求数据"""
        required_fields = ['engine', 'chart_type', 'data', 'chart_config']
        missing_fields = [field for field in required_fields if field not in data]
        
        if missing_fields:
            return False, f"缺少必需字段: {missing_fields}"
        
        if data['engine'] not in self.generators:
            return False, f"不支持的引擎: {data['engine']}"
        
        if data['chart_type'] not in self.chart_methods:
            return False, f"不支持的图表类型: {data['chart_type']}"
        
        if not isinstance(data['data'], dict) or not data['data']:
            return False, "数据必须是非空字典"
        
        return True, "验证通过"
    
    def create_chart(self, request_data, session_id=None):
        """创建图表"""
        try:
            # 验证请求
            is_valid, message = self.validate_request(request_data)
            if not is_valid:
                return {'success': False, 'error': message}
            
            # 创建会话专用输出目录
            if session_id:
                output_dir = os.path.join(app.config['OUTPUT_FOLDER'], session_id)
            else:
                output_dir = app.config['OUTPUT_FOLDER']
            
            os.makedirs(output_dir, exist_ok=True)
            
            # 获取图表生成器
            generator_class = self.generators[request_data['engine']]
            chart_gen = generator_class(output_dir=output_dir)
            
            # 获取图表方法
            method_name = self.chart_methods[request_data['chart_type']]
            method = getattr(chart_gen, method_name)
            
            # 生成图表
            chart_config = request_data['chart_config'].copy()
            
            # 如果没有指定文件名，生成一个带时间戳的文件名
            if 'filename' not in chart_config:
                timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
                engine = request_data['engine']
                chart_type = request_data['chart_type']
                extension = 'html' if engine == 'plotly' else 'png'
                chart_config['filename'] = f"{chart_type}_{timestamp}.{extension}"
            
            output_path = method(data=request_data['data'], **chart_config)
            
            # 获取相对路径和文件信息
            relative_path = os.path.relpath(output_path, app.config['OUTPUT_FOLDER'])
            file_size = os.path.getsize(output_path)
            
            return {
                'success': True,
                'output_path': output_path,
                'relative_path': relative_path,
                'filename': os.path.basename(output_path),
                'file_size': file_size,
                'chart_type': request_data['chart_type'],
                'engine': request_data['engine'],
                'session_id': session_id,
                'created_at': datetime.now().isoformat()
            }
            
        except Exception as e:
            return {
                'success': False,
                'error': str(e),
                'traceback': traceback.format_exc()
            }

# 创建API实例
chart_api = ChartAPI()

@app.route('/api/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({
        'status': 'ok',
        'service': 'PyTools Chart API',
        'version': '1.0.0',
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/engines', methods=['GET'])
def get_engines():
    """获取支持的引擎列表"""
    return jsonify({
        'engines': list(chart_api.generators.keys()),
        'chart_types': list(chart_api.chart_methods.keys())
    })

@app.route('/api/charts/create', methods=['POST'])
def create_chart():
    """创建图表"""
    try:
        if not request.is_json:
            return jsonify({'success': False, 'error': '请求必须是JSON格式'}), 400
        
        # 生成会话ID
        session_id = request.headers.get('X-Session-ID', str(uuid.uuid4()))
        
        # 处理请求
        result = chart_api.create_chart(request.json, session_id)
        
        if result['success']:
            return jsonify(result), 200
        else:
            return jsonify(result), 400
            
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e),
            'traceback': traceback.format_exc()
        }), 500

@app.route('/api/charts/batch', methods=['POST'])
def create_batch_charts():
    """批量创建图表"""
    try:
        if not request.is_json:
            return jsonify({'success': False, 'error': '请求必须是JSON格式'}), 400
        
        charts = request.json.get('charts', [])
        if not charts:
            return jsonify({'success': False, 'error': '没有提供图表配置'}), 400
        
        session_id = request.headers.get('X-Session-ID', str(uuid.uuid4()))
        results = []
        
        for i, chart_config in enumerate(charts):
            result = chart_api.create_chart(chart_config, session_id)
            result['batch_index'] = i
            results.append(result)
        
        successful = sum(1 for r in results if r['success'])
        failed = len(results) - successful
        
        return jsonify({
            'success': failed == 0,
            'total': len(results),
            'successful': successful,
            'failed': failed,
            'results': results,
            'session_id': session_id
        }), 200
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e),
            'traceback': traceback.format_exc()
        }), 500

@app.route('/api/files/<path:filename>', methods=['GET'])
def download_file(filename):
    """下载生成的文件"""
    try:
        file_path = os.path.join(app.config['OUTPUT_FOLDER'], filename)
        
        if not os.path.exists(file_path):
            return jsonify({'error': '文件不存在'}), 404
        
        # 安全检查：确保文件在输出目录内
        if not os.path.commonpath([file_path, app.config['OUTPUT_FOLDER']]) == app.config['OUTPUT_FOLDER']:
            return jsonify({'error': '非法文件路径'}), 400
        
        return send_file(file_path, as_attachment=True)
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/files/<path:filename>/info', methods=['GET'])
def get_file_info(filename):
    """获取文件信息"""
    try:
        file_path = os.path.join(app.config['OUTPUT_FOLDER'], filename)
        
        if not os.path.exists(file_path):
            return jsonify({'error': '文件不存在'}), 404
        
        stat = os.stat(file_path)
        return jsonify({
            'filename': os.path.basename(file_path),
            'size': stat.st_size,
            'created_at': datetime.fromtimestamp(stat.st_ctime).isoformat(),
            'modified_at': datetime.fromtimestamp(stat.st_mtime).isoformat(),
            'exists': True
        })
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/validate', methods=['POST'])
def validate_config():
    """验证图表配置"""
    try:
        if not request.is_json:
            return jsonify({'valid': False, 'error': '请求必须是JSON格式'}), 400
        
        is_valid, message = chart_api.validate_request(request.json)
        return jsonify({
            'valid': is_valid,
            'message' if is_valid else 'error': message
        })
        
    except Exception as e:
        return jsonify({
            'valid': False,
            'error': str(e)
        }), 500

@app.errorhandler(413)
def too_large(e):
    return jsonify({'error': '请求数据过大'}), 413

@app.errorhandler(404)
def not_found(e):
    return jsonify({'error': '端点不存在'}), 404

@app.errorhandler(500)
def internal_error(e):
    return jsonify({'error': '服务器内部错误'}), 500

def create_sample_config():
    """创建示例配置文件"""
    sample_configs = {
        'line_chart.json': {
            "engine": "matplotlib",
            "chart_type": "line",
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
        },
        'bar_chart.json': {
            "engine": "seaborn", 
            "chart_type": "bar",
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
        },
        'scatter_plot.json': {
            "engine": "plotly",
            "chart_type": "scatter",
            "data": {
                "x": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
                "y": [2, 5, 3, 8, 7, 10, 12, 6, 4, 9],
                "category": ["A", "A", "B", "B", "C", "C", "A", "B", "C", "A"]
            },
            "chart_config": {
                "x_column": "x",
                "y_column": "y",
                "title": "数据散点分布",
                "color_column": "category",
                "hover_column": "category"
            }
        }
    }
    
    # 保存示例配置到临时目录
    sample_dir = os.path.join(tempfile.gettempdir(), 'pytools_samples')
    os.makedirs(sample_dir, exist_ok=True)
    
    for filename, config in sample_configs.items():
        config_path = os.path.join(sample_dir, filename)
        with open(config_path, 'w', encoding='utf-8') as f:
            json.dump(config, f, ensure_ascii=False, indent=2)
    
    return sample_dir

if __name__ == '__main__':
    import argparse
    
    parser = argparse.ArgumentParser(description='PyTools图表API服务器')
    parser.add_argument('--host', default='0.0.0.0', help='服务器地址')
    parser.add_argument('--port', type=int, default=5000, help='服务器端口')
    parser.add_argument('--debug', action='store_true', help='调试模式')
    parser.add_argument('--output-dir', help='输出目录')
    
    args = parser.parse_args()
    
    # 设置输出目录
    if args.output_dir:
        app.config['OUTPUT_FOLDER'] = args.output_dir
        os.makedirs(args.output_dir, exist_ok=True)
    
    # 创建示例配置文件
    sample_dir = create_sample_config()
    
    print(f"PyTools图表API服务器启动")
    print(f"地址: http://{args.host}:{args.port}")
    print(f"输出目录: {app.config['OUTPUT_FOLDER']}")
    print(f"示例配置: {sample_dir}")
    print(f"健康检查: http://{args.host}:{args.port}/api/health")
    print(f"API文档: 查看代码注释了解接口详情")
    
    app.run(host=args.host, port=args.port, debug=args.debug)