import re
import sys

def parse_parser_output(input_file):
    """
    解析语法分析器的输出文件
    每行token格式为: 标识符 内容
    """
    tokens = []
    
    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except FileNotFoundError:
        print(f"错误：找不到输入文件 {input_file}")
        return None
    except Exception as e:
        print(f"读取文件时出错：{e}")
        return None
    
    for line_num, line in enumerate(lines, 1):
        line = line.strip()
        if not line:
            continue
            
        # 跳过语法成分（中括号内容）
        if line.startswith('[') and line.endswith(']'):
            continue
        
        # 处理token（标识符 内容 格式）
        # 使用split分割，第一个是标识符，其余部分是内容
        parts = line.split(None, 1)  # 只分割第一个空格
        if len(parts) == 2:
            token_type, token_content = parts
            tokens.append((token_type, token_content))
        else:
            print(f"警告：第{line_num}行格式不正确: {line}")
    
    return tokens

def reconstruct_source(tokens):
    """
    从tokens重新构建源程序
    """
    source_code = ""
    
    for token_type, content in tokens:
        # 直接拼接内容，忽略token类型
        if source_code and not source_code[-1].isspace() and not source_code[-1] in '([{':
            source_code += ' '
        source_code += content
    
    return source_code

def format_source_code(source_code):
    """
    对源代码进行简单格式化
    """
    # 首先处理for循环中的分号（不分行）
    # 使用正则表达式匹配for语句，并临时替换分号
    import re
    
    # 临时标记，用于在for语句中替换分号
    temp_marker = "___FOR_SEMICOLON___"
    
    # 匹配for语句并临时替换其中的分号
    def replace_for_semicolons(match):
        for_statement = match.group(0)
        # 将for语句中的分号替换为临时标记
        return for_statement.replace(';', temp_marker)
    
    # 查找并处理所有for语句
    for_pattern = r'for\s*\([^)]+\)'  # 匹配for(条件)模式
    source_code = re.sub(for_pattern, replace_for_semicolons, source_code)
    
    # 在分号后换行（除了for语句中的分号）
    source_code = re.sub(r';\s*', ';\n', source_code)
    
    # 恢复for语句中的分号
    source_code = source_code.replace(temp_marker, ';')
    
    # 在左大括号前后添加适当的空格和换行
    source_code = re.sub(r'\s*\{\s*', ' {\n', source_code)
    
    # 在右大括号前后添加适当的空格和换行
    source_code = re.sub(r'\s*\}\s*', '\n}\n', source_code)
    
    # 清理多余的空行
    source_code = re.sub(r'\n\s*\n', '\n', source_code)
    
    # 确保每行开头没有多余空格（除了缩进）
    lines = source_code.split('\n')
    formatted_lines = []
    
    indent_level = 0
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        # 处理右大括号的缩进减少
        if line.startswith('}'):
            indent_level = max(0, indent_level - 1)
            
        # 添加当前缩进
        indented_line = '    ' * indent_level + line
        formatted_lines.append(indented_line)
        
        # 处理左大括号的缩进增加
        if line.endswith('{'):
            indent_level += 1
    
    return '\n'.join(formatted_lines)

def process_parser_output(input_file, output_file):
    """
    主处理函数
    """
    print(f"正在处理文件: {input_file}")
    
    # 解析语法分析器输出
    tokens = parse_parser_output(input_file)
    if tokens is None:
        return False
    
    print(f"找到 {len(tokens)} 个token")
    
    # 显示token信息（可选）
    print("\nToken信息:")
    for i, (token_type, content) in enumerate(tokens[:10]):  # 只显示前10个作为示例
        print(f"  {token_type:15} {content}")
    if len(tokens) > 10:
        print(f"  ... 还有 {len(tokens) - 10} 个token")
    
    # 重新构建源程序
    source_code = reconstruct_source(tokens)
    print(f"\n重新构建的源程序长度: {len(source_code)} 字符")
    
    # 格式化源程序
    formatted_code = format_source_code(source_code)
    print(f"格式化后的源程序行数: {formatted_code.count(chr(10)) + 1}")
    
    # 保存到文件
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(formatted_code)
        print(f"结果已保存到: {output_file}")
        return True
    except Exception as e:
        print(f"保存文件时出错: {e}")
        return False

def main():
    """
    主函数
    """
    
    input_file = "parser.txt"
    output_file = "parser_re.txt"
    
    success = process_parser_output(input_file, output_file)
    
    if success:
        print("\n处理完成！")
    else:
        print("\n处理失败！")

if __name__ == "__main__":
    main()