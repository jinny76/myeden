# PyTools - Python图表生成工具库

[![Python](https://img.shields.io/badge/Python-3.8+-blue.svg)](https://www.python.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

PyTools是一个功能强大的Python图表生成工具库，基于Matplotlib、Plotly和Seaborn构建，提供统一的API接口来创建各种类型的图表并保存为图片文件。

## ✨ 特性

- 🎨 **多引擎支持**: 支持Matplotlib、Plotly和Seaborn三种绘图引擎
- 📊 **丰富图表类型**: 折线图、柱状图、饼图、散点图、热力图等
- 🎯 **统一API**: 一致的接口设计，易于使用和切换
- 🖼️ **多格式输出**: 支持PNG、HTML等多种输出格式
- 🎨 **自定义样式**: 丰富的样式和主题选择
- 📱 **交互式图表**: Plotly支持的交互式可视化
- 🔬 **科学绘图**: Seaborn专业的统计图表
- 🚀 **高性能**: 优化的渲染性能

## 📦 安装

### 使用pip安装

```bash
pip install -r requirements.txt
```

### 从源码安装

```bash
git clone <repository-url>
cd pytools
pip install -e .
```

### 开发环境安装

```bash
pip install -e ".[dev,docs,advanced]"
```

## 🚀 快速开始

### 基本使用

```python
from charts import MatplotlibCharts, PlotlyCharts, SeabornCharts
import pandas as pd

# 准备数据
data = {
    'month': ['Jan', 'Feb', 'Mar', 'Apr', 'May'],
    'sales': [100, 120, 140, 110, 160],
    'profit': [20, 25, 30, 22, 35]
}
df = pd.DataFrame(data)

# 使用Matplotlib创建折线图
matplotlib_gen = MatplotlibCharts(output_dir="output")
line_chart_path = matplotlib_gen.create_line_chart(
    data=df,
    x_column='month',
    y_columns=['sales', 'profit'],
    title='月度销售和利润趋势',
    filename='sales_trend.png'
)

print(f"图表保存至: {line_chart_path}")
```

### Plotly交互式图表

```python
# 创建交互式图表
plotly_gen = PlotlyCharts(output_dir="output", theme="plotly_white")

# 创建交互式散点图
scatter_path = plotly_gen.create_scatter_plot(
    data=df,
    x_column='sales',
    y_column='profit',
    title='销售额 vs 利润散点图',
    hover_column='month'
)
```

### Seaborn统计图表

```python
# 创建统计图表
seaborn_gen = SeabornCharts(output_dir="output", palette="Set2")

# 创建相关性矩阵
corr_path = seaborn_gen.create_correlation_matrix(
    data=df,
    title='数据相关性分析'
)
```

## 📊 支持的图表类型

### 基础图表
- **折线图** (`create_line_chart`): 显示数据随时间或其他连续变量的变化趋势
- **柱状图** (`create_bar_chart`): 比较不同类别的数值
- **饼图** (`create_pie_chart`): 显示数据的占比关系
- **散点图** (`create_scatter_plot`): 显示两个变量之间的关系
- **热力图** (`create_heatmap`): 以颜色深浅表示数据密度或相关性

### Plotly特有图表
- **3D散点图** (`create_3d_scatter`): 三维数据可视化
- **K线图** (`create_candlestick_chart`): 金融数据可视化

### Seaborn统计图表
- **相关性矩阵** (`create_correlation_matrix`): 变量间相关性分析
- **箱线图** (`create_box_plot`): 数据分布和异常值检测
- **小提琴图** (`create_violin_plot`): 数据分布密度可视化
- **分布图** (`create_distribution_plot`): 直方图与密度曲线结合

## 🎨 样式和主题

### Matplotlib样式
```python
# 可用样式: 'default', 'seaborn', 'ggplot', 'dark_background'
matplotlib_gen = MatplotlibCharts(style='dark_background')
```

### Plotly主题
```python
# 可用主题: 'plotly', 'plotly_white', 'plotly_dark', 'ggplot2', 'seaborn'
plotly_gen = PlotlyCharts(theme='plotly_dark')
```

### Seaborn样式
```python
# 样式: 'darkgrid', 'whitegrid', 'dark', 'white', 'ticks'
# 调色板: 'deep', 'muted', 'bright', 'pastel', 'dark', 'colorblind'
seaborn_gen = SeabornCharts(style='whitegrid', palette='Set2')
```

## 📁 项目结构

```
pytools/
├── src/                    # 源代码目录
│   ├── __init__.py
│   └── charts/             # 图表模块
│       ├── __init__.py
│       ├── base.py         # 基础抽象类
│       ├── matplotlib_charts.py  # Matplotlib实现
│       ├── plotly_charts.py       # Plotly实现
│       └── seaborn_charts.py      # Seaborn实现
├── examples/               # 示例代码
│   ├── basic_examples.py   # 基础示例
│   └── advanced_examples.py # 高级示例
├── tests/                  # 测试代码
│   └── test_charts.py      # 图表测试
├── output/                 # 输出目录（自动创建）
├── requirements.txt        # 依赖文件
├── setup.py               # 安装配置
└── README.md              # 说明文档
```

## 🛠️ API文档

### 基础类: ChartGenerator

所有图表生成器都继承自`ChartGenerator`基类，提供以下核心方法:

#### 必需方法
- `create_line_chart(data, x_column, y_columns, title, filename, **kwargs)`
- `create_bar_chart(data, x_column, y_column, title, filename, **kwargs)`
- `create_pie_chart(data, labels_column, values_column, title, filename, **kwargs)`
- `create_scatter_plot(data, x_column, y_column, title, filename, **kwargs)`
- `create_heatmap(data, title, filename, **kwargs)`

#### 参数说明
- `data`: 数据源，支持pandas.DataFrame或字典格式
- `x_column/y_column`: 列名字符串
- `title`: 图表标题
- `filename`: 输出文件名（可选，自动生成）
- `**kwargs`: 额外的样式参数

### 常用参数

#### 通用参数
```python
# 图片尺寸和质量
figsize=(12, 8)          # Matplotlib图片尺寸
width=1200, height=600   # Plotly图片尺寸
dpi=300                  # 图片分辨率

# 标题和标签
title_fontsize=16        # 标题字体大小
label_fontsize=12        # 坐标轴标签字体大小
xlabel='X轴标签'         # X轴标签
ylabel='Y轴标签'         # Y轴标签

# 颜色和样式
color='blue'             # 颜色
alpha=0.7                # 透明度
linewidth=2              # 线宽
marker='o'               # 标记样式
```

#### 图表特定参数

**折线图**
```python
marker='o'               # 数据点标记
linewidth=2              # 线宽
date_interval=1          # 日期间隔（日期数据时）
```

**柱状图**
```python
show_values=True         # 显示数值标签
rotate_labels=True       # 旋转X轴标签
edgecolor='navy'         # 边框颜色
```

**散点图**
```python
color_column='category'  # 颜色编码列
size_column='size'       # 大小编码列
trend_line=True          # 显示趋势线
hover_column='info'      # 悬停信息列（Plotly）
```

**饼图**
```python
explode=[0.1, 0, 0, 0]   # 突出显示某些扇形
startangle=90            # 起始角度
shadow=True              # 阴影效果
hole=0.4                 # 环形图洞的大小（Plotly）
```

**热力图**
```python
cmap='viridis'           # 颜色映射
annot=True               # 显示数值标注
center=0                 # 颜色中心值
cbar_label='Values'      # 颜色条标签
```

## 📖 示例

### 运行示例

```bash
# 运行基础示例
python examples/basic_examples.py

# 运行高级示例
python examples/advanced_examples.py
```

### 商业仪表板示例

```python
# 销售趋势分析
sales_data = {
    'month': ['1月', '2月', '3月', '4月', '5月', '6月'],
    'online_sales': [150, 180, 220, 190, 250, 280],
    'offline_sales': [200, 190, 170, 160, 180, 200],
    'target': [300, 320, 350, 330, 380, 420]
}

# 创建多线趋势图
matplotlib_gen = MatplotlibCharts()
trend_path = matplotlib_gen.create_line_chart(
    data=sales_data,
    x_column='month',
    y_columns=['online_sales', 'offline_sales', 'target'],
    title='2023年销售趋势与目标对比',
    xlabel='月份',
    ylabel='销售额（万元）',
    figsize=(15, 8),
    marker='o',
    linewidth=3
)
```

### 科学数据分析示例

```python
# 相关性分析
import numpy as np

# 生成实验数据
data = {
    'temperature': np.random.normal(25, 5, 100),
    'pressure': np.random.normal(1013, 50, 100),
    'humidity': np.random.normal(60, 15, 100),
    'reaction_rate': np.random.exponential(2, 100)
}

# 创建相关性矩阵
seaborn_gen = SeabornCharts()
corr_path = seaborn_gen.create_correlation_matrix(
    data=pd.DataFrame(data),
    title='实验变量相关性分析',
    mask_upper=True,
    cmap='RdBu_r'
)
```

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
pytest tests/ -v

# 运行特定测试文件
pytest tests/test_charts.py -v

# 运行覆盖率测试
pytest tests/ --cov=src --cov-report=html
```

### 性能测试

```python
# 运行性能测试
python tests/test_charts.py
```

## 🔧 开发

### 环境设置

```bash
# 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Linux/Mac
venv\Scripts\activate     # Windows

# 安装开发依赖
pip install -e ".[dev]"
```

### 代码规范

```bash
# 代码格式化
black src/ examples/ tests/

# 代码检查
flake8 src/ examples/ tests/
```

### 添加新的图表类型

1. 在对应的图表类中添加新方法
2. 确保方法签名与基类一致
3. 添加相应的测试用例
4. 更新文档和示例

## 📋 依赖

### 核心依赖
- matplotlib >= 3.7.0
- seaborn >= 0.12.0
- plotly >= 5.15.0
- pandas >= 2.0.0
- numpy >= 1.24.0
- Pillow >= 10.0.0
- kaleido >= 0.2.1
- scipy >= 1.10.0

### 可选依赖
- bokeh >= 3.2.0 (高级图表)
- altair >= 5.0.0 (声明式可视化)

### 开发依赖
- pytest >= 7.4.0
- pytest-cov >= 4.1.0
- black >= 23.7.0
- flake8 >= 6.0.0

## 🤝 贡献

我们欢迎各种形式的贡献!

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 贡献指南

- 请确保代码符合项目的代码规范
- 添加适当的测试用例
- 更新相关文档
- 确保所有测试通过

## 📝 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

- [Matplotlib](https://matplotlib.org/) - 强大的Python绘图库
- [Plotly](https://plotly.com/python/) - 交互式可视化库
- [Seaborn](https://seaborn.pydata.org/) - 统计数据可视化库
- [Pandas](https://pandas.pydata.org/) - 数据处理库
- [NumPy](https://numpy.org/) - 科学计算库

## 📞 联系方式

- 项目主页: [GitHub Repository](https://github.com/myeden/pytools)
- 问题反馈: [Issues](https://github.com/myeden/pytools/issues)
- 邮箱: team@myeden.com

## 🔄 更新日志

### v1.0.0 (2025-08-06)
- 🎉 首次发布
- ✨ 支持Matplotlib、Plotly、Seaborn三种引擎
- 📊 实现基础图表类型：折线图、柱状图、饼图、散点图、热力图
- 🎨 支持多种样式和主题
- 📖 提供详细的示例和文档
- 🧪 包含完整的测试套件

---

**⭐ 如果这个项目对您有帮助，请给它一个星标！**