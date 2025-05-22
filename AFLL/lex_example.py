# lex_example.py
import ply.lex as lex

reserved = {
    'if': 'IF',
    'else': 'ELSE',
    'elseif': 'ELSEIF',
    'for': 'FOR',
    'while': 'WHILE',
    'do': 'DO',
}

tokens = [
    'NAME', 'NUMBER', 'STRING', 'SINGLE_QUOTED_STRING',  
    'PLUS', 'MINUS', 'TIMES', 'DIVIDE',
    'LPAREN', 'RPAREN', 'LBRACE', 'RBRACE',
    'EQUALS', 'NOTEQ', 'LARGE', 'SMALL', 'LRGEQ', 'SMLEQ',
    'SEMICOLON', 'COMMA',
] + list(reserved.values())

t_PLUS = r'\+'
t_MINUS = r'-'
t_TIMES = r'\*'
t_DIVIDE = r'/'
t_LPAREN = r'\('
t_RPAREN = r'\)'
t_LBRACE = r'\{'
t_RBRACE = r'\}'
t_EQUALS = r'=='
t_NOTEQ = r'!='
t_LARGE = r'>'
t_SMALL = r'<'
t_LRGEQ = r'>='
t_SMLEQ = r'<='
t_SEMICOLON = r';'
t_COMMA = r','

t_ignore = ' \t'

def t_NUMBER(t):
    r'\d+'
    t.value = int(t.value)
    return t

def t_STRING(t):
    r'\"([^\\\n]|(\\.))*?\"'
    t.value = t.value[1:-1]  
    return t

def t_SINGLE_QUOTED_STRING(t):
    r"'([^\\\n]|(\\.))*?'"
    t.value = t.value[1:-1]  
    return t

def t_NAME(t):
    r'[a-zA-Z_][a-zA-Z0-9_]*'
    t.type = reserved.get(t.value, 'NAME')
    return t

def t_newline(t):
    r'\n+'
    t.lexer.lineno += t.value.count("\n")

def t_error(t):
    print("Illegal character '%s'" % t.value[0])
    t.lexer.skip(1)

lexer = lex.lex()

if __name__ == "__main__":
    lexer.input("if (x == 5) { print('x is 5'); }")
    while True:
        tok = lexer.token()
        if not tok:
            break
        print(tok)

