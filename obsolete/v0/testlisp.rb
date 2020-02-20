#!/usr/bin/ruby

require_relative 'lisp'

lisp = Lisp.new
lisp.execute('std.lisp')
lisp.repl

