"""
PyTools基本使用示例
演示如何使用各种图表生成器创建不同类型的图表
"""

import sys
import os
sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'src'))

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from charts import MatplotlibCharts, PlotlyCharts, SeabornCharts


def generate_sample_data():
    """生成示例数据"""
    # 时间序列数据
    dates = pd.date_range('2023-01-01', periods=100, freq='D')
    sales_data = {
        'date': dates,
        'product_a_sales': np.random.randint(100, 500, 100) + np.sin(np.arange(100) * 0.1) * 50,
        'product_b_sales': np.random.randint(80, 400, 100) + np.cos(np.arange(100) * 0.1) * 40,
        'temperature': np.random.normal(25, 5, 100)
    }
    
    # 分类数据
    category_data = {
        'category': ['A', 'B', 'C', 'D', 'E'],
        'values': [23, 45, 56, 78, 32],
        'percentage': [15.2, 29.7, 37.0, 51.6, 21.1]
    }
    
    # 散点数据
    scatter_data = {
        'x': np.random.randn(200),
        'y': np.random.randn(200),
        'category': np.random.choice(['Group1', 'Group2', 'Group3'], 200),
        'size': np.random.randint(10, 100, 200)
    }
    
    # 热力图数据
    heatmap_data = np.random.randn(10, 12)
    
    return sales_data, category_data, scatter_data, heatmap_data


def matplotlib_examples():
    """Matplotlib图表示例"""
    print("=== Matplotlib图表示例 ===")
    
    # 创建图表生成器
    chart_gen = MatplotlibCharts(output_dir="../output/matplotlib", style="seaborn-v0_8")
    
    # 获取示例数据
    sales_data, category_data, scatter_data, heatmap_data = generate_sample_data()
    
    # 1. 折线图
    print("创建折线图...")
    line_path = chart_gen.create_line_chart(
        data=sales_data,
        x_column='date',
        y_columns=['product_a_sales', 'product_b_sales'],
        title='产品销售趋势对比',
        xlabel='日期',
        ylabel='销售额',
        figsize=(14, 8),
        marker='o',
        linewidth=2.5
    )
    print(f"折线图保存至: {line_path}")
    
    # 2. 柱状图
    print("创建柱状图...")
    bar_path = chart_gen.create_bar_chart(
        data=category_data,
        x_column='category',
        y_column='values',
        title='各类别数值对比',
        xlabel='类别',
        ylabel='数值',
        color='lightcoral',
        show_values=True
    )
    print(f"柱状图保存至: {bar_path}")
    
    # 3. 饼图
    print("创建饼图...")
    pie_path = chart_gen.create_pie_chart(
        data=category_data,
        labels_column='category',
        values_column='values',
        title='类别占比分布',
        explode=[0.1, 0, 0, 0, 0],  # 突出显示第一个扇形
        shadow=True
    )
    print(f"饼图保存至: {pie_path}")
    
    # 4. 散点图
    print("创建散点图...")
    scatter_path = chart_gen.create_scatter_plot(
        data=scatter_data,
        x_column='x',
        y_column='y',
        title='数据点分布散点图',
        xlabel='X坐标',
        ylabel='Y坐标',
        color='purple',
        alpha=0.6,
        trend_line=True
    )
    print(f"散点图保存至: {scatter_path}")
    
    # 5. 热力图
    print("创建热力图...")
    heatmap_df = pd.DataFrame(heatmap_data, 
                             columns=[f'特征{i+1}' for i in range(12)],
                             index=[f'样本{i+1}' for i in range(10)])
    heatmap_path = chart_gen.create_heatmap(
        data=heatmap_df,
        title='数据相关性热力图',
        cmap='coolwarm',
        annot=True
    )
    print(f"热力图保存至: {heatmap_path}")


def plotly_examples():
    """Plotly交互式图表示例"""
    print("\n=== Plotly交互式图表示例 ===")
    
    # 创建图表生成器
    chart_gen = PlotlyCharts(output_dir="../output/plotly", theme="plotly_white")
    
    # 获取示例数据
    sales_data, category_data, scatter_data, heatmap_data = generate_sample_data()
    
    # 1. 交互式折线图
    print("创建交互式折线图...")
    line_path = chart_gen.create_line_chart(
        data=sales_data,
        x_column='date',
        y_columns=['product_a_sales', 'product_b_sales'],
        title='产品销售趋势对比（交互式）',
        xlabel='日期',
        ylabel='销售额',
        width=1400,
        height=700
    )
    print(f"交互式折线图保存至: {line_path}")
    
    # 2. 交互式柱状图
    print("创建交互式柱状图...")
    bar_path = chart_gen.create_bar_chart(
        data=category_data,
        x_column='category',
        y_column='values',
        title='各类别数值对比（交互式）',
        xlabel='类别',
        ylabel='数值',
        color='lightblue'
    )
    print(f"交互式柱状图保存至: {bar_path}")
    
    # 3. 交互式饼图（环形图）
    print("创建环形图...")
    pie_path = chart_gen.create_pie_chart(
        data=category_data,
        labels_column='category',
        values_column='values',
        title='类别占比分布（环形图）',
        hole=0.4  # 创建环形图
    )
    print(f"环形图保存至: {pie_path}")
    
    # 4. 带颜色编码的散点图
    print("创建带颜色编码的散点图...")
    scatter_path = chart_gen.create_scatter_plot(
        data=scatter_data,
        x_column='x',
        y_column='y',
        title='按类别着色的散点图',
        color_column='category',
        size_column='size',
        hover_column='category'
    )
    print(f"散点图保存至: {scatter_path}")
    
    # 5. 3D散点图
    print("创建3D散点图...")
    scatter_3d_data = {
        'x': np.random.randn(100),
        'y': np.random.randn(100), 
        'z': np.random.randn(100),
        'color': np.random.rand(100)
    }
    scatter_3d_path = chart_gen.create_3d_scatter(
        data=scatter_3d_data,
        x_column='x',
        y_column='y',
        z_column='z',
        title='3D数据分布',
        color_column='color',
        xlabel='X轴',
        ylabel='Y轴',
        zlabel='Z轴'
    )
    print(f"3D散点图保存至: {scatter_3d_path}")


def seaborn_examples():
    """Seaborn统计图表示例"""
    print("\n=== Seaborn统计图表示例 ===")
    
    # 创建图表生成器
    chart_gen = SeabornCharts(output_dir="../output/seaborn", style="whitegrid", palette="Set2")
    
    # 获取示例数据
    sales_data, category_data, scatter_data, heatmap_data = generate_sample_data()
    
    # 1. 统计折线图
    print("创建统计折线图...")
    line_path = chart_gen.create_line_chart(
        data=sales_data,
        x_column='date',
        y_columns=['product_a_sales', 'product_b_sales'],
        title='产品销售趋势（统计样式）',
        xlabel='日期',
        ylabel='销售额'
    )
    print(f"统计折线图保存至: {line_path}")
    
    # 2. 相关性矩阵
    print("创建相关性矩阵...")
    corr_path = chart_gen.create_correlation_matrix(
        data=pd.DataFrame(sales_data),
        title='销售数据相关性分析',
        mask_upper=True,
        cmap='RdBu_r'
    )
    print(f"相关性矩阵保存至: {corr_path}")
    
    # 3. 箱线图
    print("创建箱线图...")
    box_data = {
        'category': ['A'] * 50 + ['B'] * 50 + ['C'] * 50,
        'values': np.concatenate([
            np.random.normal(10, 2, 50),
            np.random.normal(15, 3, 50), 
            np.random.normal(12, 2.5, 50)
        ])
    }
    box_path = chart_gen.create_box_plot(
        data=box_data,
        x_column='category',
        y_column='values',
        title='不同类别数值分布箱线图'
    )
    print(f"箱线图保存至: {box_path}")
    
    # 4. 小提琴图
    print("创建小提琴图...")
    violin_path = chart_gen.create_violin_plot(
        data=box_data,
        x_column='category',
        y_column='values',
        title='不同类别数值分布小提琴图'
    )
    print(f"小提琴图保存至: {violin_path}")
    
    # 5. 分布图
    print("创建分布图...")
    dist_path = chart_gen.create_distribution_plot(
        data=sales_data,
        column='product_a_sales',
        title='产品A销售额分布',
        kde=True,
        bins=25
    )
    print(f"分布图保存至: {dist_path}")


def financial_chart_example():
    """金融图表示例（K线图）"""
    print("\n=== 金融图表示例 ===")
    
    chart_gen = PlotlyCharts(output_dir="../output/financial")
    
    # 生成模拟股价数据
    dates = pd.date_range('2023-01-01', periods=100, freq='D')
    price = 100
    prices = [price]
    
    for _ in range(99):
        change = np.random.normal(0, 2)
        price = max(price + change, 1)  # 确保价格不为负
        prices.append(price)
    
    # 生成OHLC数据
    financial_data = []
    for i, date in enumerate(dates):
        if i == 0:
            open_price = prices[i]
        else:
            open_price = financial_data[i-1]['close']
            
        high_price = open_price + abs(np.random.normal(0, 1))
        low_price = open_price - abs(np.random.normal(0, 1))
        close_price = prices[i]
        
        financial_data.append({
            'date': date,
            'open': open_price,
            'high': max(high_price, close_price, open_price),
            'low': min(low_price, close_price, open_price),
            'close': close_price
        })
    
    # 创建K线图
    candlestick_path = chart_gen.create_candlestick_chart(
        data=pd.DataFrame(financial_data),
        date_column='date',
        open_column='open',
        high_column='high',
        low_column='low',
        close_column='close',
        title='模拟股票价格K线图',
        rangeslider=True
    )
    print(f"K线图保存至: {candlestick_path}")


def main():
    """主函数"""
    print("PyTools图表生成库示例")
    print("=" * 50)
    
    # 创建输出目录
    os.makedirs("../output", exist_ok=True)
    os.makedirs("../output/matplotlib", exist_ok=True)
    os.makedirs("../output/plotly", exist_ok=True)
    os.makedirs("../output/seaborn", exist_ok=True)
    os.makedirs("../output/financial", exist_ok=True)
    
    try:
        # 运行各种示例
        matplotlib_examples()
        plotly_examples()
        seaborn_examples()
        financial_chart_example()
        
        print("\n" + "=" * 50)
        print("所有图表示例生成完成！")
        print("请查看 output/ 目录下的生成文件")
        
    except ImportError as e:
        print(f"导入错误: {e}")
        print("请确保已安装所需依赖: pip install -r requirements.txt")
    except Exception as e:
        print(f"运行错误: {e}")


if __name__ == "__main__":
    main()