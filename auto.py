import os
import subprocess
from datetime import datetime

def create_zip_with_winrar():
    """使用WinRAR创建压缩包，直接压缩src目录内容"""
    winrar_path = r"D:\WinRAR\WinRAR.exe"
    
    # 检查WinRAR是否存在
    if not os.path.exists(winrar_path):
        print(f"错误: WinRAR 未找到在指定路径: {winrar_path}")
        return False
    
    # 检查src目录是否存在
    if not os.path.exists("src"):
        print("错误: src 目录不存在")
        return False
    
    # 检查src目录是否为空
    if not os.listdir("src"):
        print("错误: src 目录为空")
        return False
    
    # 生成压缩包文件名（包含时间戳）
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    zip_filename = f"backup_{timestamp}.rar"
    
    # 构建WinRAR命令
    # a: 添加文件到压缩包
    # -r: 递归子目录
    # -ep1: 从文件名中排除基目录 - 这确保不会在压缩包中创建src目录
    cmd = [
        winrar_path,
        "a",           # 添加文件
        "-r",          # 递归子目录
        "-ep1",        # 排除基目录
        zip_filename,  # 压缩包文件名
        "src\\*"       # 要压缩的src目录下的所有内容
    ]
    
    try:
        print("正在创建压缩包...")
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=300)
        
        if result.returncode == 0:
            print(f"成功创建压缩包: {zip_filename}")
            return zip_filename
        else:
            print(f"WinRAR 执行错误: {result.stderr}")
            return False
            
    except subprocess.TimeoutExpired:
        print("错误: 压缩过程超时")
        return False
    except Exception as e:
        print(f"执行WinRAR时出错: {e}")
        return False

def main():
    print("开始执行脚本...")
    
    # 直接创建压缩包
    zip_result = create_zip_with_winrar()
    if not zip_result:
        print("创建压缩包失败")
        return
    
    print(f"\n脚本执行完成！压缩包已创建: {zip_result}")

if __name__ == "__main__":
    main()