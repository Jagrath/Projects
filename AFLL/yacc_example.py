# yacc_example.py
import ply.yacc as yacc
from lex_example import lexer, tokens

precedence = (
    ('left', 'PLUS', 'MINUS'),
    ('left', 'TIMES', 'DIVIDE'),
    ('left', 'EQUALS', 'NOTEQ', 'LARGE', 'SMALL', 'LRGEQ', 'SMLEQ'),
)

def p_statement(p):
    '''
    statement : expression
              | if_statement
              | while_statement
              | do_while_statement
              | for_statement
              | else_statement
              | print_statement
              | standalone_expression
    '''
    p[0] = p[1]

def p_standalone_expression(p):
    '''
    standalone_expression : expression SEMICOLON
    '''
    p[0] = p[1]

def p_expression(p):
    '''
    expression : expression PLUS expression
               | expression MINUS expression
               | expression TIMES expression
               | expression DIVIDE expression
               | expression EQUALS expression
               | expression NOTEQ expression
               | expression LARGE expression
               | expression SMALL expression
               | expression LRGEQ expression
               | expression SMLEQ expression
               | LPAREN expression RPAREN
               | STRING
               | SINGLE_QUOTED_STRING
               | NAME COMMA expression
    '''
    if len(p) == 4:
        p[0] = (p[2], p[1], p[3])
    else:
        p[0] = p[1]

def p_number(p):
    'expression : NUMBER'
    p[0] = p[1]

def p_name(p):
    'expression : NAME'
    p[0] = ('var', p[1])

def p_if_statement(p):
    '''
    if_statement : IF LPAREN expression RPAREN LBRACE statement RBRACE
                 | IF LPAREN expression RPAREN LBRACE statement RBRACE else_statement
                 | IF LPAREN expression RPAREN LBRACE statement RBRACE elseif_statement else_statement
    '''
    if len(p) == 8:
        p[0] = ('if', p[3], p[6])
    elif len(p) == 9 and p[8] == 'else':
        p[0] = ('ifelse', p[3], p[6], None)
    elif len(p) == 10 and p[9] == 'else':
        p[0] = ('ifelse', p[3], p[6], p[8])
    elif len(p) == 11 and p[10] == 'else':
        p[0] = ('ifelseifelse', p[3], p[6], p[8], None)
    elif len(p) == 12 and p[11] == 'else':
        p[0] = ('ifelseifelse', p[3], p[6], p[8], p[11])

def p_else_statement(p):
    '''
    else_statement : ELSE LBRACE statement RBRACE
    '''
    p[0] = p[3]

def p_elseif_statement(p):
    '''
    elseif_statement : ELSEIF LPAREN expression RPAREN LBRACE statement RBRACE
    '''
    p[0] = ('elseif', p[3], p[6])

def p_while_statement(p):
    '''
    while_statement : WHILE LPAREN expression RPAREN LBRACE statement RBRACE
    '''
    p[0] = ('while', p[3], p[6])

def p_do_while_statement(p):
    '''
    do_while_statement : DO LBRACE statement RBRACE WHILE LPAREN expression RPAREN SEMICOLON
    '''
    p[0] = ('do-while', p[3], p[7])

def p_for_statement(p):
    '''
    for_statement : FOR LPAREN expression SEMICOLON expression SEMICOLON expression RPAREN LBRACE statement RBRACE
    '''
    p[0] = ('for', p[3], p[5], p[7], p[10])

def p_print_statement(p):
    '''
    print_statement : NAME LPAREN STRING RPAREN SEMICOLON
                   | NAME LPAREN SINGLE_QUOTED_STRING RPAREN SEMICOLON
    '''
    p[0] = ('print', p[3])
    

def p_error(p):
    if p is not None:
        print("Syntax error: '%s'" % p.value)
    else:
        print("Syntax error: unexpected end of input")


# Global environment for variable storage
env = {}

def run(p):
    global env

    if type(p) == tuple:
        if p[0] == 'if':
            # Handle if statement
            if run(p[1]):
                for stmt in p[2]:
                    run(stmt)
        elif p[0] == 'ifelse':
            # Handle if-else statement
            if run(p[1]):
                for stmt in p[2]:
                    run(stmt)
            elif p[3] is not None:
                for stmt in p[3]:
                    run(stmt)
        elif p[0] == 'ifelseifelse':
            # Handle if-elseif-else statement
            if run(p[1]):
                for stmt in p[2]:
                    run(stmt)
            elif run(p[3]):
                for stmt in p[4]:
                    run(stmt)
            elif p[5] is not None:
                for stmt in p[5]:
                    run(stmt)
        elif p[0] == 'while':
            # Handle while loop
            while run(p[1]):
                for stmt in p[2]:
                    run(stmt)
        elif p[0] == 'do-while':
            # Handle do-while loop
            while True:
                for stmt in p[1]:
                    run(stmt)
                if not run(p[2]):
                    break
        elif p[0] == 'for':
            # Handle for loop
            iterable = range(run(p[1]), run(p[3]), run(p[5]))
            loop_var = p[7]
            for item in iterable:
                env[loop_var] = item
                for stmt in p[9]:
                    run(stmt)
        elif p[0] == 'print':
            # Handle print statement
            print(run(p[1]))
    elif p is not None:
        print("Result:", p)
    elif p is not None:
        if p[0] == 'print':
            print("Print Statement:", p)
            print(run(p[1]))
        else:
            print("Result:", p)
        return p
    else:
        return p


parser = yacc.yacc()

if __name__ == "__main__":
    while True:
        try:
            lines = []
            user_input = input(">> ")
            if user_input and user_input[-1] == "{":
                while True:
                    if user_input == '':
                        break
                    else:
                        lines.append(user_input + '\n')
                    user_input = input(".. ")
                s = "".join(lines)
            else:
                s = user_input
        except EOFError:
            break
        result = parser.parse(s, lexer=lexer)  # Pass the lexer to the parser
        if result:
            run(result)
