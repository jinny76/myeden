"""
基于Matplotlib的图表生成器
"""

import matplotlib.pyplot as plt
import matplotlib.dates as mdates
import seaborn as sns
import pandas as pd
import numpy as np
from typing import Optional, Dict, Any, Union, List
from pathlib import Path
from .base import ChartGenerator

# 设置中文字体支持
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False


class MatplotlibCharts(ChartGenerator):
    """基于Matplotlib的图表生成器"""
    
    def __init__(self, output_dir: str = "output", style: str = "default"):
        """
        初始化Matplotlib图表生成器
        
        Args:
            output_dir: 输出目录
            style: 图表样式 ('default', 'seaborn', 'ggplot', 'dark_background')
        """
        super().__init__(output_dir)
        self.set_style(style)
        
    def set_style(self, style: str):
        """设置图表样式"""
        if style in plt.style.available:
            plt.style.use(style)
        else:
            print(f"样式 '{style}' 不可用，使用默认样式")
    
    def create_line_chart(self, 
                         data: Union[pd.DataFrame, Dict[str, List]], 
                         x_column: str, 
                         y_columns: Union[str, List[str]],
                         title: str = "Line Chart",
                         filename: Optional[str] = None,
                         **kwargs) -> str:
        """创建折线图"""
        df = self._prepare_data(data)
        
        if isinstance(y_columns, str):
            y_columns = [y_columns]
            
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (12, 8)))
        
        for y_col in y_columns:
            ax.plot(df[x_column], df[y_col], 
                   marker=kwargs.get('marker', 'o'),
                   linewidth=kwargs.get('linewidth', 2),
                   label=y_col)
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', x_column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', 'Values'), fontsize=kwargs.get('label_fontsize', 12))
        
        # 如果x轴是日期类型，格式化显示
        if pd.api.types.is_datetime64_any_dtype(df[x_column]):
            ax.xaxis.set_major_formatter(mdates.DateFormatter('%Y-%m-%d'))
            ax.xaxis.set_major_locator(mdates.DayLocator(interval=kwargs.get('date_interval', 1)))
            plt.setp(ax.xaxis.get_majorticklabels(), rotation=45)
        
        if len(y_columns) > 1:
            ax.legend()
        
        ax.grid(True, alpha=0.3)
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("line_chart", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
    
    def create_bar_chart(self,
                        data: Union[pd.DataFrame, Dict[str, List]],
                        x_column: str,
                        y_column: str, 
                        title: str = "Bar Chart",
                        filename: Optional[str] = None,
                        **kwargs) -> str:
        """创建柱状图"""
        df = self._prepare_data(data)
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (12, 8)))
        
        bars = ax.bar(df[x_column], df[y_column], 
                     color=kwargs.get('color', 'skyblue'),
                     alpha=kwargs.get('alpha', 0.8),
                     edgecolor=kwargs.get('edgecolor', 'navy'),
                     linewidth=kwargs.get('linewidth', 1))
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', x_column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', y_column), fontsize=kwargs.get('label_fontsize', 12))
        
        # 添加数值标签
        if kwargs.get('show_values', True):
            for bar in bars:
                height = bar.get_height()
                ax.text(bar.get_x() + bar.get_width()/2., height,
                       f'{height:.1f}',
                       ha='center', va='bottom')
        
        # 旋转x轴标签
        if kwargs.get('rotate_labels', True):
            plt.setp(ax.xaxis.get_majorticklabels(), rotation=45, ha='right')
        
        ax.grid(True, alpha=0.3, axis='y')
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("bar_chart", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
    
    def create_pie_chart(self,
                        data: Union[pd.DataFrame, Dict[str, List]],
                        labels_column: str,
                        values_column: str,
                        title: str = "Pie Chart", 
                        filename: Optional[str] = None,
                        **kwargs) -> str:
        """创建饼图"""
        df = self._prepare_data(data)
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (10, 8)))
        
        colors = kwargs.get('colors', plt.cm.Set3.colors)
        explode = kwargs.get('explode', None)
        
        wedges, texts, autotexts = ax.pie(df[values_column], 
                                         labels=df[labels_column],
                                         colors=colors,
                                         explode=explode,
                                         autopct='%1.1f%%',
                                         startangle=kwargs.get('startangle', 90),
                                         shadow=kwargs.get('shadow', True))
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        
        # 美化文本
        for autotext in autotexts:
            autotext.set_color('white')
            autotext.set_fontweight('bold')
        
        plt.axis('equal')
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("pie_chart", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
    
    def create_scatter_plot(self,
                           data: Union[pd.DataFrame, Dict[str, List]], 
                           x_column: str,
                           y_column: str,
                           title: str = "Scatter Plot",
                           filename: Optional[str] = None,
                           **kwargs) -> str:
        """创建散点图"""
        df = self._prepare_data(data)
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (10, 8)))
        
        # 支持按第三个变量着色
        c_column = kwargs.get('color_column', None)
        if c_column and c_column in df.columns:
            scatter = ax.scatter(df[x_column], df[y_column], 
                               c=df[c_column], 
                               cmap=kwargs.get('cmap', 'viridis'),
                               s=kwargs.get('size', 50),
                               alpha=kwargs.get('alpha', 0.7))
            plt.colorbar(scatter, ax=ax, label=c_column)
        else:
            ax.scatter(df[x_column], df[y_column],
                      color=kwargs.get('color', 'blue'),
                      s=kwargs.get('size', 50),
                      alpha=kwargs.get('alpha', 0.7))
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', x_column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', y_column), fontsize=kwargs.get('label_fontsize', 12))
        
        # 添加趋势线
        if kwargs.get('trend_line', False):
            z = np.polyfit(df[x_column], df[y_column], 1)
            p = np.poly1d(z)
            ax.plot(df[x_column], p(df[x_column]), "r--", alpha=0.8)
        
        ax.grid(True, alpha=0.3)
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("scatter_plot", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
        
    def create_heatmap(self,
                      data: Union[pd.DataFrame, List[List]],
                      title: str = "Heatmap",
                      filename: Optional[str] = None,
                      **kwargs) -> str:
        """创建热力图"""
        if isinstance(data, list):
            df = pd.DataFrame(data)
        else:
            df = data
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (12, 8)))
        
        im = ax.imshow(df.values, 
                      cmap=kwargs.get('cmap', 'viridis'),
                      aspect=kwargs.get('aspect', 'auto'))
        
        # 设置坐标轴标签
        ax.set_xticks(np.arange(len(df.columns)))
        ax.set_yticks(np.arange(len(df.index)))
        ax.set_xticklabels(df.columns)
        ax.set_yticklabels(df.index)
        
        # 旋转x轴标签
        plt.setp(ax.get_xticklabels(), rotation=45, ha="right")
        
        # 添加数值标注
        if kwargs.get('annot', True):
            for i in range(len(df.index)):
                for j in range(len(df.columns)):
                    text = ax.text(j, i, f'{df.iloc[i, j]:.2f}',
                                 ha="center", va="center", color="white")
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        
        # 添加颜色条
        cbar = plt.colorbar(im, ax=ax)
        cbar.ax.set_ylabel(kwargs.get('cbar_label', 'Values'), rotation=-90, va="bottom")
        
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("heatmap", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
        
    def create_histogram(self,
                        data: Union[pd.DataFrame, Dict[str, List]],
                        column: str,
                        title: str = "Histogram",
                        filename: Optional[str] = None,
                        **kwargs) -> str:
        """创建直方图"""
        df = self._prepare_data(data)
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (10, 6)))
        
        ax.hist(df[column], 
               bins=kwargs.get('bins', 30),
               color=kwargs.get('color', 'skyblue'),
               alpha=kwargs.get('alpha', 0.7),
               edgecolor=kwargs.get('edgecolor', 'black'))
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', 'Frequency'), fontsize=kwargs.get('label_fontsize', 12))
        
        ax.grid(True, alpha=0.3)
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("histogram", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)