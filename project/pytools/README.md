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
matplotlib_gen = MatplotlibCharts(output_dir=\"output\")\nline_chart_path = matplotlib_gen.create_line_chart(\n    data=df,\n    x_column='month',\n    y_columns=['sales', 'profit'],\n    title='月度销售和利润趋势',\n    filename='sales_trend.png'\n)\n\nprint(f\"图表保存至: {line_chart_path}\")\n```\n\n### Plotly交互式图表\n\n```python\n# 创建交互式图表\nplotly_gen = PlotlyCharts(output_dir=\"output\", theme=\"plotly_white\")\n\n# 创建交互式散点图\nscatter_path = plotly_gen.create_scatter_plot(\n    data=df,\n    x_column='sales',\n    y_column='profit',\n    title='销售额 vs 利润散点图',\n    hover_column='month'\n)\n```\n\n### Seaborn统计图表\n\n```python\n# 创建统计图表\nseaborn_gen = SeabornCharts(output_dir=\"output\", palette=\"Set2\")\n\n# 创建相关性矩阵\ncorr_path = seaborn_gen.create_correlation_matrix(\n    data=df,\n    title='数据相关性分析'\n)\n```\n\n## 📊 支持的图表类型\n\n### 基础图表\n- **折线图** (`create_line_chart`): 显示数据随时间或其他连续变量的变化趋势\n- **柱状图** (`create_bar_chart`): 比较不同类别的数值\n- **饼图** (`create_pie_chart`): 显示数据的占比关系\n- **散点图** (`create_scatter_plot`): 显示两个变量之间的关系\n- **热力图** (`create_heatmap`): 以颜色深浅表示数据密度或相关性\n\n### Plotly特有图表\n- **3D散点图** (`create_3d_scatter`): 三维数据可视化\n- **K线图** (`create_candlestick_chart`): 金融数据可视化\n\n### Seaborn统计图表\n- **相关性矩阵** (`create_correlation_matrix`): 变量间相关性分析\n- **箱线图** (`create_box_plot`): 数据分布和异常值检测\n- **小提琴图** (`create_violin_plot`): 数据分布密度可视化\n- **分布图** (`create_distribution_plot`): 直方图与密度曲线结合\n\n## 🎨 样式和主题\n\n### Matplotlib样式\n```python\n# 可用样式: 'default', 'seaborn', 'ggplot', 'dark_background'\nmatplotlib_gen = MatplotlibCharts(style='dark_background')\n```\n\n### Plotly主题\n```python\n# 可用主题: 'plotly', 'plotly_white', 'plotly_dark', 'ggplot2', 'seaborn'\nplotly_gen = PlotlyCharts(theme='plotly_dark')\n```\n\n### Seaborn样式\n```python\n# 样式: 'darkgrid', 'whitegrid', 'dark', 'white', 'ticks'\n# 调色板: 'deep', 'muted', 'bright', 'pastel', 'dark', 'colorblind'\nseaborn_gen = SeabornCharts(style='whitegrid', palette='Set2')\n```\n\n## 📁 项目结构\n\n```\npytools/\n├── src/                    # 源代码目录\n│   ├── __init__.py\n│   └── charts/             # 图表模块\n│       ├── __init__.py\n│       ├── base.py         # 基础抽象类\n│       ├── matplotlib_charts.py  # Matplotlib实现\n│       ├── plotly_charts.py       # Plotly实现\n│       └── seaborn_charts.py      # Seaborn实现\n├── examples/               # 示例代码\n│   ├── basic_examples.py   # 基础示例\n│   └── advanced_examples.py # 高级示例\n├── tests/                  # 测试代码\n│   └── test_charts.py      # 图表测试\n├── output/                 # 输出目录（自动创建）\n├── requirements.txt        # 依赖文件\n├── setup.py               # 安装配置\n└── README.md              # 说明文档\n```\n\n## 🛠️ API文档\n\n### 基础类: ChartGenerator\n\n所有图表生成器都继承自`ChartGenerator`基类，提供以下核心方法:\n\n#### 必需方法\n- `create_line_chart(data, x_column, y_columns, title, filename, **kwargs)`\n- `create_bar_chart(data, x_column, y_column, title, filename, **kwargs)`\n- `create_pie_chart(data, labels_column, values_column, title, filename, **kwargs)`\n- `create_scatter_plot(data, x_column, y_column, title, filename, **kwargs)`\n- `create_heatmap(data, title, filename, **kwargs)`\n\n#### 参数说明\n- `data`: 数据源，支持pandas.DataFrame或字典格式\n- `x_column/y_column`: 列名字符串\n- `title`: 图表标题\n- `filename`: 输出文件名（可选，自动生成）\n- `**kwargs`: 额外的样式参数\n\n### 常用参数\n\n#### 通用参数\n```python\n# 图片尺寸和质量\nfigsize=(12, 8)          # Matplotlib图片尺寸\nwidth=1200, height=600   # Plotly图片尺寸\ndpi=300                  # 图片分辨率\n\n# 标题和标签\ntitle_fontsize=16        # 标题字体大小\nlabel_fontsize=12        # 坐标轴标签字体大小\nxlabel='X轴标签'         # X轴标签\nylabel='Y轴标签'         # Y轴标签\n\n# 颜色和样式\ncolor='blue'             # 颜色\nalpha=0.7                # 透明度\nlinewidth=2              # 线宽\nmarker='o'               # 标记样式\n```\n\n#### 图表特定参数\n\n**折线图**\n```python\nmarker='o'               # 数据点标记\nlinewidth=2              # 线宽\ndate_interval=1          # 日期间隔（日期数据时）\n```\n\n**柱状图**\n```python\nshow_values=True         # 显示数值标签\nrotate_labels=True       # 旋转X轴标签\nedgecolor='navy'         # 边框颜色\n```\n\n**散点图**\n```python\ncolor_column='category'  # 颜色编码列\nsize_column='size'       # 大小编码列\ntrend_line=True          # 显示趋势线\nhover_column='info'      # 悬停信息列（Plotly）\n```\n\n**饼图**\n```python\nexplode=[0.1, 0, 0, 0]   # 突出显示某些扇形\nstartangle=90            # 起始角度\nshadow=True              # 阴影效果\nhole=0.4                 # 环形图洞的大小（Plotly）\n```\n\n**热力图**\n```python\ncmap='viridis'           # 颜色映射\nannot=True               # 显示数值标注\ncenter=0                 # 颜色中心值\ncbar_label='Values'      # 颜色条标签\n```\n\n## 📖 示例\n\n### 运行示例\n\n```bash\n# 运行基础示例\npython examples/basic_examples.py\n\n# 运行高级示例\npython examples/advanced_examples.py\n```\n\n### 商业仪表板示例\n\n```python\n# 销售趋势分析\nsales_data = {\n    'month': ['1月', '2月', '3月', '4月', '5月', '6月'],\n    'online_sales': [150, 180, 220, 190, 250, 280],\n    'offline_sales': [200, 190, 170, 160, 180, 200],\n    'target': [300, 320, 350, 330, 380, 420]\n}\n\n# 创建多线趋势图\nmatplotlib_gen = MatplotlibCharts()\ntrend_path = matplotlib_gen.create_line_chart(\n    data=sales_data,\n    x_column='month',\n    y_columns=['online_sales', 'offline_sales', 'target'],\n    title='2023年销售趋势与目标对比',\n    xlabel='月份',\n    ylabel='销售额（万元）',\n    figsize=(15, 8),\n    marker='o',\n    linewidth=3\n)\n```\n\n### 科学数据分析示例\n\n```python\n# 相关性分析\nimport numpy as np\n\n# 生成实验数据\ndata = {\n    'temperature': np.random.normal(25, 5, 100),\n    'pressure': np.random.normal(1013, 50, 100),\n    'humidity': np.random.normal(60, 15, 100),\n    'reaction_rate': np.random.exponential(2, 100)\n}\n\n# 创建相关性矩阵\nseaborn_gen = SeabornCharts()\ncorr_path = seaborn_gen.create_correlation_matrix(\n    data=pd.DataFrame(data),\n    title='实验变量相关性分析',\n    mask_upper=True,\n    cmap='RdBu_r'\n)\n```\n\n## 🧪 测试\n\n### 运行测试\n\n```bash\n# 运行所有测试\npytest tests/ -v\n\n# 运行特定测试文件\npytest tests/test_charts.py -v\n\n# 运行覆盖率测试\npytest tests/ --cov=src --cov-report=html\n```\n\n### 性能测试\n\n```python\n# 运行性能测试\npython tests/test_charts.py\n```\n\n## 🔧 开发\n\n### 环境设置\n\n```bash\n# 创建虚拟环境\npython -m venv venv\nsource venv/bin/activate  # Linux/Mac\nvenv\\Scripts\\activate     # Windows\n\n# 安装开发依赖\npip install -e \".[dev]\"\n```\n\n### 代码规范\n\n```bash\n# 代码格式化\nblack src/ examples/ tests/\n\n# 代码检查\nflake8 src/ examples/ tests/\n```\n\n### 添加新的图表类型\n\n1. 在对应的图表类中添加新方法\n2. 确保方法签名与基类一致\n3. 添加相应的测试用例\n4. 更新文档和示例\n\n## 📋 依赖\n\n### 核心依赖\n- matplotlib >= 3.7.0\n- seaborn >= 0.12.0\n- plotly >= 5.15.0\n- pandas >= 2.0.0\n- numpy >= 1.24.0\n- Pillow >= 10.0.0\n- kaleido >= 0.2.1\n- scipy >= 1.10.0\n\n### 可选依赖\n- bokeh >= 3.2.0 (高级图表)\n- altair >= 5.0.0 (声明式可视化)\n\n### 开发依赖\n- pytest >= 7.4.0\n- pytest-cov >= 4.1.0\n- black >= 23.7.0\n- flake8 >= 6.0.0\n\n## 🤝 贡献\n\n我们欢迎各种形式的贡献!\n\n1. Fork项目\n2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)\n3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)\n4. 推送到分支 (`git push origin feature/AmazingFeature`)\n5. 开启Pull Request\n\n### 贡献指南\n\n- 请确保代码符合项目的代码规范\n- 添加适当的测试用例\n- 更新相关文档\n- 确保所有测试通过\n\n## 📝 许可证\n\n本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情\n\n## 🙏 致谢\n\n- [Matplotlib](https://matplotlib.org/) - 强大的Python绘图库\n- [Plotly](https://plotly.com/python/) - 交互式可视化库\n- [Seaborn](https://seaborn.pydata.org/) - 统计数据可视化库\n- [Pandas](https://pandas.pydata.org/) - 数据处理库\n- [NumPy](https://numpy.org/) - 科学计算库\n\n## 📞 联系方式\n\n- 项目主页: [GitHub Repository](https://github.com/myeden/pytools)\n- 问题反馈: [Issues](https://github.com/myeden/pytools/issues)\n- 邮箱: team@myeden.com\n\n## 🔄 更新日志\n\n### v1.0.0 (2025-08-06)\n- 🎉 首次发布\n- ✨ 支持Matplotlib、Plotly、Seaborn三种引擎\n- 📊 实现基础图表类型：折线图、柱状图、饼图、散点图、热力图\n- 🎨 支持多种样式和主题\n- 📖 提供详细的示例和文档\n- 🧪 包含完整的测试套件\n\n---\n\n**⭐ 如果这个项目对您有帮助，请给它一个星标！**