"""
基于Seaborn的统计图表生成器
"""

import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
import numpy as np
from typing import Optional, Dict, Any, Union, List
from pathlib import Path
from .base import ChartGenerator

# 设置中文字体支持
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False


class SeabornCharts(ChartGenerator):
    """基于Seaborn的统计图表生成器"""
    
    def __init__(self, output_dir: str = "output", style: str = "whitegrid", palette: str = "deep"):
        """
        初始化Seaborn图表生成器
        
        Args:
            output_dir: 输出目录
            style: 图表样式 ('darkgrid', 'whitegrid', 'dark', 'white', 'ticks')
            palette: 颜色调色板
        """
        super().__init__(output_dir)
        sns.set_style(style)
        sns.set_palette(palette)
        
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
            sns.lineplot(data=df, x=x_column, y=y_col, 
                        marker=kwargs.get('marker', 'o'),
                        linewidth=kwargs.get('linewidth', 2),
                        label=y_col, ax=ax)
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', x_column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', 'Values'), fontsize=kwargs.get('label_fontsize', 12))
        
        if len(y_columns) > 1:
            ax.legend()
        
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
        
        sns.barplot(data=df, x=x_column, y=y_column, ax=ax,
                   palette=kwargs.get('palette', None))
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', x_column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', y_column), fontsize=kwargs.get('label_fontsize', 12))
        
        # 旋转x轴标签
        if kwargs.get('rotate_labels', True):
            plt.setp(ax.xaxis.get_majorticklabels(), rotation=45, ha='right')
            
        # 添加数值标签
        if kwargs.get('show_values', True):
            for container in ax.containers:
                ax.bar_label(container, fmt='%.1f')
        
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
        """创建饼图（Seaborn没有直接的饼图，使用matplotlib）"""
        df = self._prepare_data(data)
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (10, 8)))
        
        # 使用seaborn的颜色
        colors = sns.color_palette(kwargs.get('palette', 'Set2'), len(df))
        
        wedges, texts, autotexts = ax.pie(df[values_column], 
                                         labels=df[labels_column],
                                         colors=colors,
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
        
        # 支持按第三个变量着色和大小
        hue_column = kwargs.get('hue_column', None)
        size_column = kwargs.get('size_column', None)
        
        sns.scatterplot(data=df, x=x_column, y=y_column,
                       hue=hue_column,
                       size=size_column,
                       sizes=kwargs.get('sizes', (20, 200)),
                       alpha=kwargs.get('alpha', 0.7),
                       ax=ax)
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', x_column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', y_column), fontsize=kwargs.get('label_fontsize', 12))
        
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
        
        sns.heatmap(df, 
                   annot=kwargs.get('annot', True),
                   cmap=kwargs.get('cmap', 'viridis'),
                   center=kwargs.get('center', None),
                   square=kwargs.get('square', False),
                   cbar_kws={'label': kwargs.get('cbar_label', 'Values')},
                   ax=ax)
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("heatmap", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
        
    def create_correlation_matrix(self,
                                 data: Union[pd.DataFrame, Dict[str, List]],
                                 title: str = "Correlation Matrix",
                                 filename: Optional[str] = None,
                                 **kwargs) -> str:
        """创建相关性矩阵热力图"""
        df = self._prepare_data(data)
        
        # 只选择数值列
        numeric_df = df.select_dtypes(include=[np.number])
        corr_matrix = numeric_df.corr()
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (10, 8)))
        
        # 创建遮罩以只显示下三角
        if kwargs.get('mask_upper', True):
            mask = np.triu(np.ones_like(corr_matrix, dtype=bool))
        else:
            mask = None
        
        sns.heatmap(corr_matrix,
                   mask=mask,
                   annot=kwargs.get('annot', True),
                   cmap=kwargs.get('cmap', 'coolwarm'),
                   center=0,
                   square=True,
                   cbar_kws={'label': 'Correlation Coefficient'},
                   ax=ax)
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("correlation_matrix", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
        
    def create_box_plot(self,
                       data: Union[pd.DataFrame, Dict[str, List]],
                       x_column: Optional[str],
                       y_column: str,
                       title: str = "Box Plot",
                       filename: Optional[str] = None,
                       **kwargs) -> str:
        """创建箱线图"""
        df = self._prepare_data(data)
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (10, 6)))
        
        sns.boxplot(data=df, x=x_column, y=y_column,
                   palette=kwargs.get('palette', None),
                   ax=ax)
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', x_column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', y_column), fontsize=kwargs.get('label_fontsize', 12))
        
        if x_column and kwargs.get('rotate_labels', True):
            plt.setp(ax.xaxis.get_majorticklabels(), rotation=45, ha='right')
        
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("box_plot", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
        
    def create_violin_plot(self,
                          data: Union[pd.DataFrame, Dict[str, List]],
                          x_column: Optional[str],
                          y_column: str,
                          title: str = "Violin Plot",
                          filename: Optional[str] = None,
                          **kwargs) -> str:
        """创建小提琴图"""
        df = self._prepare_data(data)
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (10, 6)))
        
        sns.violinplot(data=df, x=x_column, y=y_column,
                      palette=kwargs.get('palette', None),
                      inner=kwargs.get('inner', 'box'),
                      ax=ax)
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', x_column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', y_column), fontsize=kwargs.get('label_fontsize', 12))
        
        if x_column and kwargs.get('rotate_labels', True):
            plt.setp(ax.xaxis.get_majorticklabels(), rotation=45, ha='right')
        
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("violin_plot", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)
        
    def create_distribution_plot(self,
                               data: Union[pd.DataFrame, Dict[str, List]],
                               column: str,
                               title: str = "Distribution Plot",
                               filename: Optional[str] = None,
                               **kwargs) -> str:
        """创建分布图（直方图+密度曲线）"""
        df = self._prepare_data(data)
        
        fig, ax = plt.subplots(figsize=kwargs.get('figsize', (10, 6)))
        
        sns.histplot(data=df, x=column, 
                    kde=kwargs.get('kde', True),
                    bins=kwargs.get('bins', 30),
                    ax=ax)
        
        ax.set_title(title, fontsize=kwargs.get('title_fontsize', 16), fontweight='bold')
        ax.set_xlabel(kwargs.get('xlabel', column), fontsize=kwargs.get('label_fontsize', 12))
        ax.set_ylabel(kwargs.get('ylabel', 'Frequency'), fontsize=kwargs.get('label_fontsize', 12))
        
        plt.tight_layout()
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("distribution_plot", title)
        output_path = self.get_output_path(filename)
        
        plt.savefig(output_path, dpi=kwargs.get('dpi', 300), bbox_inches='tight')
        plt.close()
        
        return str(output_path)