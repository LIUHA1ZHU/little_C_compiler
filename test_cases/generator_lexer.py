import random
import os

# 定义token映射表
tokens = {
    # 标识符和常量
    "Ident": "IDENFR",
    "IntConst": "INTCON", 
    "StringConst": "STRCON",
    
    # 关键字
    "const": "CONSTTK",
    "int": "INTTK",
    "static": "STATICTK", 
    "break": "BREAKTK",
    "continue": "CONTINUETK",
    "if": "IFTK",
    "main": "MAINTK",
    "printf": "PRINTFTK",
    "else": "ELSETK",
    "for": "FORTK", 
    "return": "RETURNTK",
    "void": "VOIDTK",
    
    # 运算符
    "!": "NOT",
    "&&": "AND", 
    "||": "OR",
    "+": "PLUS",
    "-": "MINU",
    "*": "MULT",
    "/": "DIV",
    "%": "MOD",
    "<": "LSS",
    "<=": "LEQ",
    ">": "GRE", 
    ">=": "GEQ",
    "==": "EQL",
    "!=": "NEQ",
    "=": "ASSIGN",
    
    # 分隔符
    ";": "SEMICN",
    ",": "COMMA", 
    "(": "LPARENT",
    ")": "RPARENT",
    "[": "LBRACK",
    "]": "RBRACK",
    "{": "LBRACE",
    "}": "RBRACE"
}

# 特殊token需要特殊处理的内容生成函数
def generate_ident():
    """生成随机标识符"""
    first_char = random.choice('abcdefghijklmnopqrstuvwxyz_')
    rest_chars = ''.join(random.choices('abcdefghijklmnopqrstuvwxyz0123456789_', k=random.randint(1, 8)))
    return first_char + rest_chars

def generate_int_const():
    """生成随机整数常量"""
    return str(random.randint(0, 10000))

def generate_string_const():
    """生成随机字符串常量"""
    chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 '
    length = random.randint(1, 20)
    return '"' + ''.join(random.choices(chars, k=length)) + '"'

def needs_space_before(token_type):
    """判断在某个token前是否需要空格"""
    # 关键字和标识符前通常需要空格，除非前面是某些分隔符
    return token_type in ["Ident", "IntConst", "StringConst", 
                         "const", "int", "static", "break", "continue", 
                         "if", "main", "printf", "else", "for", "return", "void"]

def needs_space_after(token_type):
    """判断在某个token后是否需要空格"""
    # 关键字后通常需要空格，除非后面是某些分隔符
    return token_type in ["const", "int", "static", "break", "continue", 
                         "if", "main", "printf", "else", "for", "return", "void"]

def is_separator(token_type):
    """判断是否是分隔符"""
    return token_type in [";", ",", "(", ")", "[", "]", "{", "}"]

def generate_token_sequence(num_tokens=100):
    """生成随机token序列"""
    token_list = []
    identification_codes = []
    
    # 获取所有可能的token名称
    all_tokens = list(tokens.keys())
    
    for i in range(num_tokens):
        # 随机选择一个token类型
        token_type = random.choice(all_tokens)
        identification_code = tokens[token_type]
        
        # 根据token类型生成具体内容
        if token_type == "Ident":
            content = generate_ident()
        elif token_type == "IntConst":
            content = generate_int_const() 
        elif token_type == "StringConst":
            content = generate_string_const()
        else:
            content = token_type  # 其他token直接使用名称作为内容
            
        token_list.append(content)
        identification_codes.append((identification_code, content))
    
    return token_list, identification_codes

def save_results(token_list, identification_codes):
    """保存结果到文件"""
    
    # 保存token序列到testfile_r.txt（智能添加空格和换行）
    with open("testfile_r.txt", "w", encoding="utf-8") as f:
        result = ""
        
        for i, token in enumerate(token_list):
            # 如果是第一个token，直接添加
            if i == 0:
                result += token
                continue
                
            # 获取前一个token的类型
            prev_token = token_list[i-1]
            
            # 判断是否需要在前一个token后添加空格
            if (needs_space_after(prev_token) and not is_separator(token)) or \
               (needs_space_before(token) and not is_separator(prev_token)):
                result += " " + token
            else:
                result += token  # 直接连接，不加空格
            
            # 随机决定是否插入换行符（大约每5-10个token插入一个换行）
            if random.random() < 0.15:  # 15%的概率插入换行
                result += "\n"
            # 在特定token后更可能插入换行
            elif token in [";", "{", "}"] and random.random() < 0.4:
                result += "\n"
        
        f.write(result)
    
    # 保存识别码和内容到ans.txt
    with open("ans.txt", "w", encoding="utf-8") as f:
        for code, content in identification_codes:
            f.write(f"{code} {content}\n")

def main():
    # 设置随机种子以便结果可重现（可选）
    # random.seed(42)
    
    # 生成100个token的序列
    print("正在生成随机token序列...")
    token_list, identification_codes = generate_token_sequence(1000)
    
    # 保存结果
    save_results(token_list, identification_codes)
    
    print("生成完成！")
    print(f"共生成 {len(token_list)} 个token")
    print("结果已保存到：")
    print("  - testfile_r.txt (智能添加空格的token序列，含随机换行)")
    print("  - ans.txt (识别码和内容)")
    
    # 显示前10个token作为示例
    print("\n前10个token示例：")
    for i, (code, content) in enumerate(identification_codes[:10]):
        print(f"  {i+1:2d}. {code:10s} {content}")
    
    # 显示生成的文件内容预览
    print("\n生成的文件内容预览:")
    with open("testfile_r.txt", "r", encoding="utf-8") as f:
        content = f.read()
        # 显示前200个字符
        print("testfile_r.txt (前200字符):")
        print(repr(content[:200]))

if __name__ == "__main__":
    main()