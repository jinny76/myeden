"""
图表生成器基类
"""

import os
from abc import ABC, abstractmethod
from typing import Optional, Dict, Any, Union, List
from pathlib import Path
import pandas as pd


class ChartGenerator(ABC):
    """图表生成器基类"""
    
    def __init__(self, output_dir: str = "output"):
        """
        初始化图表生成器
        
        Args:
            output_dir: 图片输出目录
        """
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        
    @abstractmethod
    def create_line_chart(self, 
                         data: Union[pd.DataFrame, Dict[str, List]], 
                         x_column: str, 
                         y_columns: Union[str, List[str]],
                         title: str = "Line Chart",
                         filename: Optional[str] = None,
                         **kwargs) -> str:
        """创建折线图"""
        pass
    
    @abstractmethod 
    def create_bar_chart(self,
                        data: Union[pd.DataFrame, Dict[str, List]],
                        x_column: str,
                        y_column: str, 
                        title: str = "Bar Chart",
                        filename: Optional[str] = None,
                        **kwargs) -> str:
        """创建柱状图"""
        pass
    
    @abstractmethod
    def create_pie_chart(self,
                        data: Union[pd.DataFrame, Dict[str, List]],
                        labels_column: str,
                        values_column: str,
                        title: str = "Pie Chart", 
                        filename: Optional[str] = None,
                        **kwargs) -> str:
        """创建饼图"""
        pass
    
    @abstractmethod
    def create_scatter_plot(self,
                           data: Union[pd.DataFrame, Dict[str, List]], 
                           x_column: str,
                           y_column: str,
                           title: str = "Scatter Plot",
                           filename: Optional[str] = None,
                           **kwargs) -> str:
        """创建散点图"""
        pass
        
    @abstractmethod
    def create_heatmap(self,
                      data: Union[pd.DataFrame, List[List]],
                      title: str = "Heatmap",
                      filename: Optional[str] = None,
                      **kwargs) -> str:
        """创建热力图"""
        pass
        
    def _prepare_data(self, data: Union[pd.DataFrame, Dict[str, List]]) -> pd.DataFrame:
        """
        准备数据，统一转换为DataFrame格式
        
        Args:
            data: 输入数据
            
        Returns:
            DataFrame格式的数据
        """
        if isinstance(data, dict):
            return pd.DataFrame(data)
        elif isinstance(data, pd.DataFrame):
            return data
        else:
            raise ValueError("数据格式不支持，请使用DataFrame或字典格式")
    
    def _generate_filename(self, chart_type: str, title: str, extension: str = "png") -> str:
        """
        生成文件名
        
        Args:
            chart_type: 图表类型
            title: 图表标题
            extension: 文件扩展名
            
        Returns:
            生成的文件名
        """
        import re
        from datetime import datetime
        
        # 清理标题，移除特殊字符
        clean_title = re.sub(r'[^\w\s-]', '', title).strip()
        clean_title = re.sub(r'[-\s]+', '_', clean_title)
        
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        return f"{chart_type}_{clean_title}_{timestamp}.{extension}"
        
    def get_output_path(self, filename: str) -> Path:
        """
        获取完整的输出路径
        
        Args:
            filename: 文件名
            
        Returns:
            完整的输出路径
        """
        return self.output_dir / filename