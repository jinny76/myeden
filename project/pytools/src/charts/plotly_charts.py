"""
基于Plotly的交互式图表生成器
"""

import plotly.graph_objects as go
import plotly.express as px
from plotly.subplots import make_subplots
import pandas as pd
import numpy as np
from typing import Optional, Dict, Any, Union, List
from pathlib import Path
from .base import ChartGenerator


class PlotlyCharts(ChartGenerator):
    """基于Plotly的交互式图表生成器"""
    
    def __init__(self, output_dir: str = "output", theme: str = "plotly"):
        """
        初始化Plotly图表生成器
        
        Args:
            output_dir: 输出目录
            theme: 图表主题 ('plotly', 'plotly_white', 'plotly_dark', 'ggplot2', 'seaborn', 'simple_white')
        """
        super().__init__(output_dir)
        self.theme = theme
        
    def create_line_chart(self, 
                         data: Union[pd.DataFrame, Dict[str, List]], 
                         x_column: str, 
                         y_columns: Union[str, List[str]],
                         title: str = "Line Chart",
                         filename: Optional[str] = None,
                         **kwargs) -> str:
        """创建交互式折线图"""
        df = self._prepare_data(data)
        
        if isinstance(y_columns, str):
            y_columns = [y_columns]
        
        fig = go.Figure()
        
        for y_col in y_columns:
            fig.add_trace(go.Scatter(
                x=df[x_column],
                y=df[y_col],
                mode=kwargs.get('mode', 'lines+markers'),
                name=y_col,
                line=dict(width=kwargs.get('line_width', 2)),
                marker=dict(size=kwargs.get('marker_size', 6))
            ))
        
        fig.update_layout(
            title=dict(
                text=title,
                x=0.5,
                font=dict(size=kwargs.get('title_fontsize', 20))
            ),
            xaxis_title=kwargs.get('xlabel', x_column),
            yaxis_title=kwargs.get('ylabel', 'Values'),
            template=self.theme,
            hovermode='x unified',
            width=kwargs.get('width', 1200),
            height=kwargs.get('height', 600)
        )
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("line_chart", title, "html")
        output_path = self.get_output_path(filename)
        
        # 同时保存HTML和PNG格式
        fig.write_html(str(output_path))
        
        # 如果需要PNG格式
        if kwargs.get('save_png', True):
            png_path = output_path.with_suffix('.png')
            fig.write_image(str(png_path), width=kwargs.get('width', 1200), height=kwargs.get('height', 600))
        
        return str(output_path)
    
    def create_bar_chart(self,
                        data: Union[pd.DataFrame, Dict[str, List]],
                        x_column: str,
                        y_column: str, 
                        title: str = "Bar Chart",
                        filename: Optional[str] = None,
                        **kwargs) -> str:
        """创建交互式柱状图"""
        df = self._prepare_data(data)
        
        fig = go.Figure()
        
        fig.add_trace(go.Bar(
            x=df[x_column],
            y=df[y_column],
            name=y_column,
            marker_color=kwargs.get('color', 'lightblue'),
            text=df[y_column] if kwargs.get('show_values', True) else None,
            textposition='auto'
        ))
        
        fig.update_layout(
            title=dict(
                text=title,
                x=0.5,
                font=dict(size=kwargs.get('title_fontsize', 20))
            ),
            xaxis_title=kwargs.get('xlabel', x_column),
            yaxis_title=kwargs.get('ylabel', y_column),
            template=self.theme,
            width=kwargs.get('width', 1000),
            height=kwargs.get('height', 600)
        )
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("bar_chart", title, "html")
        output_path = self.get_output_path(filename)
        
        fig.write_html(str(output_path))
        
        if kwargs.get('save_png', True):
            png_path = output_path.with_suffix('.png')
            fig.write_image(str(png_path), width=kwargs.get('width', 1000), height=kwargs.get('height', 600))
        
        return str(output_path)
    
    def create_pie_chart(self,
                        data: Union[pd.DataFrame, Dict[str, List]],
                        labels_column: str,
                        values_column: str,
                        title: str = "Pie Chart", 
                        filename: Optional[str] = None,
                        **kwargs) -> str:
        """创建交互式饼图"""
        df = self._prepare_data(data)
        
        fig = go.Figure()
        
        fig.add_trace(go.Pie(
            labels=df[labels_column],
            values=df[values_column],
            hole=kwargs.get('hole', 0),  # 0为饼图，0.3为环形图
            textinfo='label+percent',
            textposition='auto'
        ))
        
        fig.update_layout(
            title=dict(
                text=title,
                x=0.5,
                font=dict(size=kwargs.get('title_fontsize', 20))
            ),
            template=self.theme,
            width=kwargs.get('width', 800),
            height=kwargs.get('height', 600)
        )
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("pie_chart", title, "html")
        output_path = self.get_output_path(filename)
        
        fig.write_html(str(output_path))
        
        if kwargs.get('save_png', True):
            png_path = output_path.with_suffix('.png')
            fig.write_image(str(png_path), width=kwargs.get('width', 800), height=kwargs.get('height', 600))
        
        return str(output_path)
    
    def create_scatter_plot(self,
                           data: Union[pd.DataFrame, Dict[str, List]], 
                           x_column: str,
                           y_column: str,
                           title: str = "Scatter Plot",
                           filename: Optional[str] = None,
                           **kwargs) -> str:
        """创建交互式散点图"""
        df = self._prepare_data(data)
        
        fig = go.Figure()
        
        # 支持按第三个变量着色和大小
        color_column = kwargs.get('color_column', None)
        size_column = kwargs.get('size_column', None)
        
        fig.add_trace(go.Scatter(
            x=df[x_column],
            y=df[y_column],
            mode='markers',
            marker=dict(
                size=df[size_column] if size_column and size_column in df.columns else kwargs.get('marker_size', 8),
                color=df[color_column] if color_column and color_column in df.columns else kwargs.get('color', 'blue'),
                colorscale=kwargs.get('colorscale', 'viridis') if color_column else None,
                showscale=True if color_column else False,
                colorbar=dict(title=color_column) if color_column else None,
                opacity=kwargs.get('opacity', 0.7)
            ),
            text=df[kwargs.get('hover_column')] if kwargs.get('hover_column') else None,
            hovertemplate='%{text}<br>X: %{x}<br>Y: %{y}<extra></extra>' if kwargs.get('hover_column') else None
        ))
        
        fig.update_layout(
            title=dict(
                text=title,
                x=0.5,
                font=dict(size=kwargs.get('title_fontsize', 20))
            ),
            xaxis_title=kwargs.get('xlabel', x_column),
            yaxis_title=kwargs.get('ylabel', y_column),
            template=self.theme,
            width=kwargs.get('width', 1000),
            height=kwargs.get('height', 600)
        )
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("scatter_plot", title, "html")
        output_path = self.get_output_path(filename)
        
        fig.write_html(str(output_path))
        
        if kwargs.get('save_png', True):
            png_path = output_path.with_suffix('.png')
            fig.write_image(str(png_path), width=kwargs.get('width', 1000), height=kwargs.get('height', 600))
        
        return str(output_path)
        
    def create_heatmap(self,
                      data: Union[pd.DataFrame, List[List]],
                      title: str = "Heatmap",
                      filename: Optional[str] = None,
                      **kwargs) -> str:
        """创建交互式热力图"""
        if isinstance(data, list):
            df = pd.DataFrame(data)
        else:
            df = data
            
        fig = go.Figure()
        
        fig.add_trace(go.Heatmap(
            z=df.values,
            x=df.columns.tolist(),
            y=df.index.tolist(),
            colorscale=kwargs.get('colorscale', 'viridis'),
            text=df.values if kwargs.get('show_text', True) else None,
            texttemplate='%{text:.2f}' if kwargs.get('show_text', True) else None,
            showscale=True,
            colorbar=dict(title=kwargs.get('colorbar_title', 'Values'))
        ))
        
        fig.update_layout(
            title=dict(
                text=title,
                x=0.5,
                font=dict(size=kwargs.get('title_fontsize', 20))
            ),
            template=self.theme,
            width=kwargs.get('width', 1000),
            height=kwargs.get('height', 600)
        )
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("heatmap", title, "html")
        output_path = self.get_output_path(filename)
        
        fig.write_html(str(output_path))
        
        if kwargs.get('save_png', True):
            png_path = output_path.with_suffix('.png')
            fig.write_image(str(png_path), width=kwargs.get('width', 1000), height=kwargs.get('height', 600))
        
        return str(output_path)
        
    def create_3d_scatter(self,
                         data: Union[pd.DataFrame, Dict[str, List]],
                         x_column: str,
                         y_column: str,
                         z_column: str,
                         title: str = "3D Scatter Plot",
                         filename: Optional[str] = None,
                         **kwargs) -> str:
        """创建3D散点图"""
        df = self._prepare_data(data)
        
        fig = go.Figure()
        
        color_column = kwargs.get('color_column', None)
        
        fig.add_trace(go.Scatter3d(
            x=df[x_column],
            y=df[y_column],
            z=df[z_column],
            mode='markers',
            marker=dict(
                size=kwargs.get('marker_size', 5),
                color=df[color_column] if color_column and color_column in df.columns else kwargs.get('color', 'blue'),
                colorscale=kwargs.get('colorscale', 'viridis') if color_column else None,
                showscale=True if color_column else False,
                opacity=kwargs.get('opacity', 0.8)
            ),
            text=df[kwargs.get('hover_column')] if kwargs.get('hover_column') else None
        ))
        
        fig.update_layout(
            title=dict(
                text=title,
                x=0.5,
                font=dict(size=kwargs.get('title_fontsize', 20))
            ),
            scene=dict(
                xaxis_title=kwargs.get('xlabel', x_column),
                yaxis_title=kwargs.get('ylabel', y_column),
                zaxis_title=kwargs.get('zlabel', z_column)
            ),
            template=self.theme,
            width=kwargs.get('width', 1000),
            height=kwargs.get('height', 800)
        )
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("3d_scatter", title, "html")
        output_path = self.get_output_path(filename)
        
        fig.write_html(str(output_path))
        
        if kwargs.get('save_png', True):
            png_path = output_path.with_suffix('.png')
            fig.write_image(str(png_path), width=kwargs.get('width', 1000), height=kwargs.get('height', 800))
        
        return str(output_path)
        
    def create_candlestick_chart(self,
                               data: Union[pd.DataFrame, Dict[str, List]],
                               date_column: str,
                               open_column: str,
                               high_column: str,
                               low_column: str,
                               close_column: str,
                               title: str = "Candlestick Chart",
                               filename: Optional[str] = None,
                               **kwargs) -> str:
        """创建K线图（蜡烛图）"""
        df = self._prepare_data(data)
        
        fig = go.Figure()
        
        fig.add_trace(go.Candlestick(
            x=df[date_column],
            open=df[open_column],
            high=df[high_column],
            low=df[low_column],
            close=df[close_column],
            name='Price'
        ))
        
        fig.update_layout(
            title=dict(
                text=title,
                x=0.5,
                font=dict(size=kwargs.get('title_fontsize', 20))
            ),
            xaxis_title=kwargs.get('xlabel', 'Date'),
            yaxis_title=kwargs.get('ylabel', 'Price'),
            template=self.theme,
            xaxis_rangeslider_visible=kwargs.get('rangeslider', False),
            width=kwargs.get('width', 1200),
            height=kwargs.get('height', 600)
        )
        
        # 保存文件
        if not filename:
            filename = self._generate_filename("candlestick", title, "html")
        output_path = self.get_output_path(filename)
        
        fig.write_html(str(output_path))
        
        if kwargs.get('save_png', True):
            png_path = output_path.with_suffix('.png')
            fig.write_image(str(png_path), width=kwargs.get('width', 1200), height=kwargs.get('height', 600))
        
        return str(output_path)