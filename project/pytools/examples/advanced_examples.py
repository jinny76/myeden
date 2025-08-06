"""
PyTools高级使用示例
展示更复杂的图表创建和自定义功能
"""

import sys
import os
sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'src'))

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from charts import MatplotlibCharts, PlotlyCharts, SeabornCharts


def create_business_dashboard_data():
    """创建商业仪表板数据"""
    # 销售数据
    months = ['1月', '2月', '3月', '4月', '5月', '6月', 
              '7月', '8月', '9月', '10月', '11月', '12月']
    
    sales_data = {
        'month': months,
        'online_sales': [150, 180, 220, 190, 250, 280, 320, 300, 270, 290, 310, 350],
        'offline_sales': [200, 190, 170, 160, 180, 200, 210, 220, 230, 240, 220, 260],
        'target': [300, 320, 350, 330, 380, 420, 450, 460, 440, 470, 480, 500]
    }
    
    # 地区销售数据
    regional_data = {
        'region': ['华北', '华东', '华南', '华中', '西南', '西北', '东北'],
        'sales_q1': [120, 230, 180, 150, 90, 60, 80],
        'sales_q2': [140, 250, 200, 170, 110, 75, 95],
        'sales_q3': [160, 280, 220, 190, 130, 85, 105],
        'sales_q4': [180, 320, 250, 210, 150, 95, 115]
    }
    
    # 客户满意度数据
    satisfaction_data = {
        'category': ['产品质量', '服务态度', '价格合理性', '物流速度', '售后服务'],
        'satisfaction_score': [4.2, 4.5, 3.8, 4.1, 3.9],
        'importance': [4.8, 4.6, 4.2, 4.3, 4.4]
    }
    
    return sales_data, regional_data, satisfaction_data


def create_scientific_data():
    """创建科学研究数据"""
    # 实验数据
    np.random.seed(42)
    n_samples = 200
    
    experiment_data = {
        'temperature': np.random.normal(25, 5, n_samples),
        'pressure': np.random.normal(1013, 50, n_samples),
        'humidity': np.random.normal(60, 15, n_samples),
        'reaction_rate': np.random.exponential(2, n_samples),
        'experiment_group': np.random.choice(['控制组', '实验组A', '实验组B'], n_samples)
    }
    
    # 添加一些相关性
    experiment_data['reaction_rate'] += (
        experiment_data['temperature'] * 0.1 + 
        experiment_data['pressure'] * 0.001 - 
        experiment_data['humidity'] * 0.05 + 
        np.random.normal(0, 0.5, n_samples)
    )
    
    return experiment_data


def business_dashboard_example():
    """商业仪表板示例"""
    print("=== 商业仪表板示例 ===")
    
    sales_data, regional_data, satisfaction_data = create_business_dashboard_data()
    
    # 1. 销售趋势对比图（Matplotlib）
    matplotlib_gen = MatplotlibCharts(output_dir="../output/business", style="seaborn-v0_8")
    
    print("创建销售趋势对比图...")
    trend_path = matplotlib_gen.create_line_chart(
        data=sales_data,
        x_column='month',
        y_columns=['online_sales', 'offline_sales', 'target'],
        title='2023年销售趋势与目标对比',
        xlabel='月份',
        ylabel='销售额（万元）',
        figsize=(15, 8),
        marker='o',
        linewidth=3,
        title_fontsize=18
    )
    print(f"销售趋势图保存至: {trend_path}")
    
    # 2. 地区销售热力图（Seaborn）
    seaborn_gen = SeabornCharts(output_dir="../output/business", palette="YlOrRd")
    
    print("创建地区销售热力图...")
    regional_df = pd.DataFrame(regional_data).set_index('region')
    heatmap_path = seaborn_gen.create_heatmap(
        data=regional_df,
        title='各地区季度销售业绩热力图',
        cmap='YlOrRd',
        annot=True,
        cbar_label='销售额（万元）'
    )
    print(f"地区销售热力图保存至: {heatmap_path}")
    
    # 3. 客户满意度散点图（Plotly）
    plotly_gen = PlotlyCharts(output_dir="../output/business", theme="plotly_white")
    
    print("创建客户满意度分析图...")
    satisfaction_path = plotly_gen.create_scatter_plot(
        data=satisfaction_data,
        x_column='importance',
        y_column='satisfaction_score',
        title='客户满意度vs重要性分析',
        xlabel='重要性评分',
        ylabel='满意度评分',
        hover_column='category',
        marker_size=15,
        color='lightcoral'
    )
    print(f"客户满意度分析图保存至: {satisfaction_path}")


def scientific_analysis_example():
    """科学数据分析示例"""
    print("\n=== 科学数据分析示例 ===")
    
    experiment_data = create_scientific_data()
    
    seaborn_gen = SeabornCharts(output_dir="../output/scientific", 
                               style="whitegrid", palette="Set1")
    
    # 1. 多变量相关性分析
    print("创建变量相关性矩阵...")
    experiment_df = pd.DataFrame(experiment_data)
    numeric_df = experiment_df.select_dtypes(include=[np.number])
    
    corr_path = seaborn_gen.create_correlation_matrix(
        data=numeric_df,
        title='实验变量相关性分析',
        mask_upper=True,
        cmap='RdBu_r',
        figsize=(10, 8)
    )
    print(f"相关性矩阵保存至: {corr_path}")
    
    # 2. 分组箱线图
    print("创建分组反应率箱线图...")
    box_path = seaborn_gen.create_box_plot(
        data=experiment_data,
        x_column='experiment_group',
        y_column='reaction_rate',
        title='不同实验组反应率分布对比',
        xlabel='实验组',
        ylabel='反应率',
        figsize=(10, 6)
    )
    print(f"分组箱线图保存至: {box_path}")
    
    # 3. 小提琴图
    print("创建温度分布小提琴图...")
    violin_path = seaborn_gen.create_violin_plot(
        data=experiment_data,
        x_column='experiment_group',
        y_column='temperature',
        title='不同实验组温度分布密度',
        xlabel='实验组',
        ylabel='温度（°C）',
        inner='box'
    )
    print(f"小提琴图保存至: {violin_path}")
    
    # 4. 3D散点图（Plotly）
    plotly_gen = PlotlyCharts(output_dir="../output/scientific")
    
    print("创建3D实验数据散点图...")
    scatter_3d_path = plotly_gen.create_3d_scatter(
        data=experiment_data,
        x_column='temperature',
        y_column='pressure',
        z_column='humidity',
        title='实验条件3D分布',
        color_column='reaction_rate',
        xlabel='温度（°C）',
        ylabel='压力（hPa）',
        zlabel='湿度（%）',
        colorscale='viridis'
    )
    print(f"3D散点图保存至: {scatter_3d_path}")


def time_series_analysis_example():
    """时间序列分析示例"""
    print("\n=== 时间序列分析示例 ===")
    
    # 生成时间序列数据
    dates = pd.date_range('2022-01-01', periods=365, freq='D')
    
    # 创建包含趋势、季节性和噪声的时间序列
    trend = np.linspace(100, 200, 365)
    seasonal = 20 * np.sin(2 * np.pi * np.arange(365) / 365.25 * 4)  # 季节性
    noise = np.random.normal(0, 5, 365)
    
    time_series_data = {
        'date': dates,
        'value': trend + seasonal + noise,
        'trend': trend,
        'seasonal': seasonal,
        'moving_avg_7': pd.Series(trend + seasonal + noise).rolling(window=7).mean(),
        'moving_avg_30': pd.Series(trend + seasonal + noise).rolling(window=30).mean()
    }
    
    # 创建时间序列图
    plotly_gen = PlotlyCharts(output_dir="../output/timeseries")
    
    print("创建时间序列分析图...")
    ts_path = plotly_gen.create_line_chart(
        data=time_series_data,
        x_column='date',
        y_columns=['value', 'moving_avg_7', 'moving_avg_30'],
        title='时间序列数据与移动平均线',
        xlabel='日期',
        ylabel='数值',
        width=1400,
        height=700
    )
    print(f"时间序列分析图保存至: {ts_path}")


def custom_styling_example():
    """自定义样式示例"""
    print("\n=== 自定义样式示例 ===")
    
    # 创建示例数据
    data = {
        'category': ['产品A', '产品B', '产品C', '产品D', '产品E'],
        'sales_2022': [120, 150, 90, 200, 180],
        'sales_2023': [140, 170, 110, 220, 200],
        'growth_rate': [16.7, 13.3, 22.2, 10.0, 11.1]
    }
    
    # 使用不同样式创建图表
    matplotlib_gen = MatplotlibCharts(output_dir="../output/custom", style="dark_background")
    
    print("创建暗色主题柱状图...")
    dark_bar_path = matplotlib_gen.create_bar_chart(
        data=data,
        x_column='category',
        y_column='sales_2023',
        title='2023年产品销售业绩（暗色主题）',
        xlabel='产品类别',
        ylabel='销售额（万元）',
        color='cyan',
        alpha=0.8,
        edgecolor='white',
        linewidth=2,
        show_values=True,
        figsize=(12, 8)
    )
    print(f"暗色主题柱状图保存至: {dark_bar_path}")
    
    # Plotly主题示例
    plotly_gen = PlotlyCharts(output_dir="../output/custom", theme="plotly_dark")
    
    print("创建暗色主题散点图...")
    dark_scatter_path = plotly_gen.create_scatter_plot(
        data=data,
        x_column='sales_2022',
        y_column='sales_2023',
        title='2022 vs 2023年销售业绩对比（暗色主题）',
        xlabel='2022年销售额（万元）',
        ylabel='2023年销售额（万元）',
        hover_column='category',
        marker_size=12,
        color='gold'
    )
    print(f"暗色主题散点图保存至: {dark_scatter_path}")


def main():
    """主函数"""
    print("PyTools高级图表示例")
    print("=" * 60)
    
    # 创建输出目录
    for subdir in ['business', 'scientific', 'timeseries', 'custom']:
        os.makedirs(f"../output/{subdir}", exist_ok=True)
    
    try:
        # 运行高级示例
        business_dashboard_example()
        scientific_analysis_example()
        time_series_analysis_example()
        custom_styling_example()
        
        print("\n" + "=" * 60)
        print("所有高级图表示例生成完成！")
        print("这些示例展示了:")
        print("- 商业仪表板图表")
        print("- 科学数据分析图表") 
        print("- 时间序列分析")
        print("- 自定义样式和主题")
        print("\n请查看 output/ 目录下的各个子文件夹")
        
    except ImportError as e:
        print(f"导入错误: {e}")
        print("请确保已安装所需依赖: pip install -r requirements.txt")
    except Exception as e:
        print(f"运行错误: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()