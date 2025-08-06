"""
PyTools图表生成器测试
"""

import sys
import os
import tempfile
import shutil
import pandas as pd
import numpy as np
import pytest
from pathlib import Path

# 添加src路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'src'))

from charts import MatplotlibCharts, PlotlyCharts, SeabornCharts


class TestChartGenerators:
    """图表生成器测试类"""
    
    @classmethod
    def setup_class(cls):
        """设置测试类"""
        cls.temp_dir = tempfile.mkdtemp()
        cls.sample_data = {
            'x': [1, 2, 3, 4, 5],
            'y1': [10, 20, 15, 25, 30],
            'y2': [5, 15, 10, 20, 25],
            'category': ['A', 'B', 'C', 'D', 'E'],
            'values': [23, 45, 56, 78, 32]
        }
        cls.sample_df = pd.DataFrame(cls.sample_data)
        
    @classmethod
    def teardown_class(cls):
        """清理测试类"""
        shutil.rmtree(cls.temp_dir)
    
    def test_matplotlib_charts_initialization(self):
        """测试Matplotlib图表生成器初始化"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        assert chart_gen.output_dir == Path(self.temp_dir)
        assert chart_gen.output_dir.exists()
    
    def test_matplotlib_line_chart(self):
        """测试Matplotlib折线图生成"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        
        # 测试单条线
        output_path = chart_gen.create_line_chart(
            data=self.sample_df,
            x_column='x',
            y_columns='y1',
            title='Test Line Chart',
            filename='test_line_single.png'
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_line_single.png')
        
        # 测试多条线
        output_path = chart_gen.create_line_chart(
            data=self.sample_df,
            x_column='x',
            y_columns=['y1', 'y2'],
            title='Test Multi Line Chart',
            filename='test_line_multi.png'
        )
        
        assert os.path.exists(output_path)
    
    def test_matplotlib_bar_chart(self):
        """测试Matplotlib柱状图生成"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        
        output_path = chart_gen.create_bar_chart(
            data=self.sample_df,
            x_column='category',
            y_column='values',
            title='Test Bar Chart',
            filename='test_bar.png'
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_bar.png')
    
    def test_matplotlib_pie_chart(self):
        """测试Matplotlib饼图生成"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        
        output_path = chart_gen.create_pie_chart(
            data=self.sample_df,
            labels_column='category',
            values_column='values',
            title='Test Pie Chart',
            filename='test_pie.png'
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_pie.png')
    
    def test_matplotlib_scatter_plot(self):
        """测试Matplotlib散点图生成"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        
        output_path = chart_gen.create_scatter_plot(
            data=self.sample_df,
            x_column='x',
            y_column='y1',
            title='Test Scatter Plot',
            filename='test_scatter.png'
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_scatter.png')
    
    def test_matplotlib_heatmap(self):
        """测试Matplotlib热力图生成"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        
        # 创建测试数据矩阵
        heatmap_data = pd.DataFrame(np.random.randn(5, 5))
        
        output_path = chart_gen.create_heatmap(
            data=heatmap_data,
            title='Test Heatmap',
            filename='test_heatmap.png'
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_heatmap.png')
    
    def test_plotly_charts_initialization(self):
        """测试Plotly图表生成器初始化"""
        chart_gen = PlotlyCharts(output_dir=self.temp_dir)
        assert chart_gen.output_dir == Path(self.temp_dir)
        assert chart_gen.theme == "plotly"
    
    def test_plotly_line_chart(self):
        """测试Plotly折线图生成"""
        chart_gen = PlotlyCharts(output_dir=self.temp_dir)
        
        output_path = chart_gen.create_line_chart(
            data=self.sample_df,
            x_column='x',
            y_columns=['y1', 'y2'],
            title='Test Plotly Line Chart',
            filename='test_plotly_line.html',
            save_png=False  # 跳过PNG保存以加快测试
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_plotly_line.html')
    
    def test_plotly_bar_chart(self):
        """测试Plotly柱状图生成"""
        chart_gen = PlotlyCharts(output_dir=self.temp_dir)
        
        output_path = chart_gen.create_bar_chart(
            data=self.sample_df,
            x_column='category',
            y_column='values',
            title='Test Plotly Bar Chart',
            filename='test_plotly_bar.html',
            save_png=False
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_plotly_bar.html')
    
    def test_seaborn_charts_initialization(self):
        """测试Seaborn图表生成器初始化"""
        chart_gen = SeabornCharts(output_dir=self.temp_dir)
        assert chart_gen.output_dir == Path(self.temp_dir)
    
    def test_seaborn_line_chart(self):
        """测试Seaborn折线图生成"""
        chart_gen = SeabornCharts(output_dir=self.temp_dir)
        
        output_path = chart_gen.create_line_chart(
            data=self.sample_df,
            x_column='x',
            y_columns='y1',
            title='Test Seaborn Line Chart',
            filename='test_seaborn_line.png'
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_seaborn_line.png')
    
    def test_seaborn_correlation_matrix(self):
        """测试Seaborn相关性矩阵"""
        chart_gen = SeabornCharts(output_dir=self.temp_dir)
        
        # 创建包含更多数值列的数据
        corr_data = pd.DataFrame({
            'var1': np.random.randn(50),
            'var2': np.random.randn(50),
            'var3': np.random.randn(50),
            'var4': np.random.randn(50)
        })
        
        output_path = chart_gen.create_correlation_matrix(
            data=corr_data,
            title='Test Correlation Matrix',
            filename='test_correlation.png'
        )
        
        assert os.path.exists(output_path)
        assert output_path.endswith('test_correlation.png')
    
    def test_data_preparation(self):
        """测试数据准备功能"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        
        # 测试字典数据转换
        dict_data = {'a': [1, 2, 3], 'b': [4, 5, 6]}
        df = chart_gen._prepare_data(dict_data)
        assert isinstance(df, pd.DataFrame)
        assert list(df.columns) == ['a', 'b']
        
        # 测试DataFrame数据
        df_data = pd.DataFrame(dict_data)
        df = chart_gen._prepare_data(df_data)
        assert isinstance(df, pd.DataFrame)
        
        # 测试无效数据类型
        with pytest.raises(ValueError):
            chart_gen._prepare_data([1, 2, 3])  # 列表格式不支持
    
    def test_filename_generation(self):
        """测试文件名生成"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        
        filename = chart_gen._generate_filename("line_chart", "Test Chart")
        assert filename.startswith("line_chart_Test_Chart")
        assert filename.endswith(".png")
        
        # 测试特殊字符处理
        filename = chart_gen._generate_filename("bar_chart", "Chart with Special: Chars!")
        assert ":" not in filename
        assert "!" not in filename
    
    def test_output_path_generation(self):
        """测试输出路径生成"""
        chart_gen = MatplotlibCharts(output_dir=self.temp_dir)
        
        path = chart_gen.get_output_path("test.png")
        assert str(path).endswith("test.png")
        assert str(self.temp_dir) in str(path)


class TestDataHandling:
    """数据处理测试类"""
    
    def test_empty_data_handling(self):
        """测试空数据处理"""
        chart_gen = MatplotlibCharts(output_dir=tempfile.mkdtemp())
        
        # 测试空DataFrame
        empty_df = pd.DataFrame()
        
        with pytest.raises(Exception):  # 应该抛出异常
            chart_gen.create_line_chart(
                data=empty_df,
                x_column='x',
                y_columns='y',
                title='Empty Data Test'
            )
    
    def test_missing_columns_handling(self):
        """测试缺失列处理"""
        chart_gen = MatplotlibCharts(output_dir=tempfile.mkdtemp())
        
        data = pd.DataFrame({'a': [1, 2, 3], 'b': [4, 5, 6]})
        
        with pytest.raises(KeyError):  # 应该抛出键错误
            chart_gen.create_line_chart(
                data=data,
                x_column='missing_column',
                y_columns='b',
                title='Missing Column Test'
            )
    
    def test_data_type_handling(self):
        """测试不同数据类型处理"""
        chart_gen = MatplotlibCharts(output_dir=tempfile.mkdtemp())
        
        # 测试包含字符串的数值列
        mixed_data = pd.DataFrame({
            'x': [1, 2, 3, 4, 5],
            'y': ['10', '20', '15', '25', '30']  # 字符串数字
        })
        
        # 转换为数值类型
        mixed_data['y'] = pd.to_numeric(mixed_data['y'])
        
        try:
            output_path = chart_gen.create_line_chart(
                data=mixed_data,
                x_column='x',
                y_columns='y',
                title='Mixed Data Types Test'
            )
            assert os.path.exists(output_path)
        except Exception as e:
            pytest.fail(f"处理混合数据类型失败: {e}")


def run_performance_test():
    """运行性能测试"""
    print("=== 性能测试 ===")
    import time
    
    # 生成大数据集
    large_data = {
        'x': list(range(1000)),
        'y': np.random.randn(1000),
        'category': ['A'] * 500 + ['B'] * 500
    }
    
    chart_gen = MatplotlibCharts(output_dir=tempfile.mkdtemp())
    
    # 测试折线图性能
    start_time = time.time()
    output_path = chart_gen.create_line_chart(
        data=large_data,
        x_column='x',
        y_columns='y',
        title='Performance Test Line Chart'
    )
    end_time = time.time()
    
    print(f"生成1000点折线图用时: {end_time - start_time:.2f}秒")
    print(f"输出文件: {output_path}")
    
    # 测试散点图性能
    start_time = time.time()
    output_path = chart_gen.create_scatter_plot(
        data=large_data,
        x_column='x',
        y_column='y',
        title='Performance Test Scatter Plot'
    )
    end_time = time.time()
    
    print(f"生成1000点散点图用时: {end_time - start_time:.2f}秒")
    print(f"输出文件: {output_path}")


if __name__ == "__main__":
    # 运行测试
    pytest.main([__file__, "-v"])
    
    # 运行性能测试
    run_performance_test()