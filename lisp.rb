class Env < Hash
	attr_reader :outer

	def initialize(outer = nil, args = nil, vals = nil)
		args.each_index do |i|
			if args[i].to_s.start_with?('*')
				self[args[i].to_s[1..-1].intern] = (vals[i].kind_of?(Array) and vals.length - 1 == i) ? vals[i] : vals[i..-1]
				break
			else
				self[args[i]] = vals[i] unless vals[i].nil?
			end
		end unless args.nil? or vals.nil?

		@outer = outer
	end

	def find(arg)
		return has_key?(arg) ? self : @outer.find(arg) rescue raise "Variable #{arg} not found!"
	end

	def self.get_global
		env = Env.new
		env.merge!({
			:add	 => lambda { |a, b| a + b },
			:sub	 => lambda { |a, b| a - b },
			:mul	 => lambda { |a, b| a * b },
			:pow	 => lambda { |a, b| a ** b },
			:div	 => lambda { |a, b| a / b },
			:mod	 => lambda { |a, b| a % b },
			:not	 => lambda { |a| return (a.nil? or (a == false) or (a == 0)) ? true : false },
			:eq		 => lambda { |a, b| a == b },
			:ne		 => lambda { |a, b| a != b },
			:gt		 => lambda { |a, b| a > b },
			:lt		 => lambda { |a, b| a < b },
			:ge		 => lambda { |a, b| a >= b },
			:le		 => lambda { |a, b| a <= b },
			:length  => lambda { |a| a.size },
			:cons	 => lambda { |a, b| [a] + b },
			:car	 => lambda { |a| a[0] },
			:cdr	 => lambda { |a| a[1..-1] },
			:append	 => lambda { |a, b| b << a },
			:list	 => lambda { |*a| a },
			:list?	 => lambda { |a| a.kind_of?(Array) },
			:null?	 => lambda { |a| a.nil? },
			:empty?	 => lambda { |a| a.empty? },
			:symbol? => lambda { |a| a.kind_of?(Symbol) },
			:quote	 => lambda { |a| a },
			:sin	 => lambda { |a| Math::sin(a) },
			:cos	 => lambda { |a| Math::cos(a) },
			:tan	 => lambda { |a| Math::tan(a) },
			:asin	 => lambda { |a| Math::asin(a) },
			:acos	 => lambda { |a| Math::acos(a) },
			:atan	 => lambda { |a| Math::atan(a) },
			:and	 => lambda { |a, b| [a, b].each do |v| return false if v.nil? or v == false or v == 0 or (v.kind_of?(Array) and v.empty?) end; true },
			:or		 => lambda { |a, b| [a, b].each do |v| return true unless v.nil? or v == false or v == 0 or (v.kind_of?(Array) and v.empty?) end; false },
			:ceil	 => lambda { |a| a.ceil },
			:floor	 => lambda { |a| a.floor },
			:round	 => lambda { |a| a.round },
			:print	 => lambda { |a| puts Lisp.to_string(a) },
			:rand	 => lambda { rand },
			:PI		 => Math::PI,
			:pi		 => Math::PI,
			:E		 => Math::E,
			:e		 => Math::E,
			:t		 => true,
			:f		 => false
			})

		return env
	end
end

class Lisp
	def initialize
		@env = Env.get_global
	end

	def evaluate(s, env = @env)
		if s.kind_of?(Symbol)
			if s.to_s.start_with?('\'')
				return s.to_s[1..-1].intern
			else
				return env.find(s)[s]
			end
		elsif not s.kind_of?(Array)
			return s
		elsif s[0] == :quote
			s.shift
			s.each_index do |i|
				s[i] = evaluate([:quote] + s[i], env) if s[i].kind_of?(Array)
			end
			return s
		elsif s[0] == :eval
			return evaluate(evaluate(s[1], env), env)
		elsif s[0] == 'fn-hash'.intern
			return env.find(s[1])[s[1]]
		elsif s[0] == :if
			name, cond, cons, alt = s 
			return evaluate(evaluate(cond, env) ? cons : alt, env)
		elsif s[0] == :cond
			s[1..-1].each do |st|
				return evaluate(st[1], env) if evaluate(st[0], env)
			end
			return []
		elsif s[0] == :load
			s[1..-1].each do |fn|
				execute(fn)
			end
			return []
		elsif s[0] == :set!
			name, id, val = s
			env.find(id)[id] = evaluate(val, env)
			return []
		elsif s[0] == :define
			name, id, val = s
			env[evaluate(id, env)] = evaluate(val, env)
			return []
		elsif s[0] == :defun
			name, id, args, body = s
			env[id] = evaluate([:lambda, args, body], env)
			return []
		elsif s[0] == :lambda
			name, args, body = s
			return lambda { |*params| evaluate(body, Env.new(env, args, params)) }
		elsif s[0] == :begin
			val = []
			s[1..-1].each do |expr|
				val = evaluate(expr, env)
			end
			return val
		elsif s[0] == :let
			name, args, ex = s

			env = Env.new(env)
			args.each do |arg|
				env[arg[0]] = evaluate(arg[1], env)
			end

			return evaluate(ex, env)
		elsif s[0] == 'env-make'.intern
			args = s[1]

			env = Env.new(env)
			args.each do |arg|
				env[arg[0]] = evaluate(arg[1], env)
			end unless args.nil?
		
			return env;
		elsif s[0] == 'env-outer'.intern
			return env.outer;
		elsif s[0] == 'env-set!'.intern
			e = evaluate(s[1], env);
			if e.kind_of?(Env)
				@env = e
				return true
			else
				return false
			end
		elsif s[0] == 'env-use'.intern
			args = s[2]
			e = Env.new(evaluate(s[1], env))
			
			args.each do |arg|
				if arg.kind_of?(Array)
					e[arg[0]] = evaluate(arg[1], env)
				else
					e[arg] = evaluate(arg, env)
				end
			end unless args.empty?

			return evaluate(s[3], e);
		else
			exprs = []
			s.each do |expr|
				exprs << evaluate(expr, env)
			end
			fn = exprs.shift
			return fn.call(*exprs)
		end
	end

	def interpret(s)
		e = evaluate(tokenize(s))
		@env['$'.intern] = e	
		puts '=> ' + Lisp.to_string(e)
	end

	def execute(fn)
		input = nil

		File.open(fn, "r") do |file|
			input = file.read
		end

		unless input.nil?
			input.gsub!(/\n+/, '').gsub!(/[ \t]+/, ' ')
			puts '> ' + input
			interpret(input)
		end
	end

	def self.to_string(ev)
		if ev.kind_of?(Array)
			ret = '('
			ev.each_index do |i|
				ret += Lisp.to_string(ev[i])
				ret += ' ' if i < ev.size - 1
			end
			return ret + ')'
		else
			return ev.to_s
		end
	end

	def repl(msg = 'lisp> ')
		while true
			print msg
			val = gets
			val.gsub!(/\n+/, '')
			break if(val == 'q')
			interpret(val) rescue puts $!.message
		end
	end

	def tokenize(s)
		s.gsub!(/'\(/, '(quote ')
		s.gsub!(/([\(\)])/, ' \1 ')
		s.gsub!(/^[ \t\n]+/, '')
		s.gsub!(/[ \t\n]+$/, '')
		
		return read_from(s.split(/[ \t]+/))
#	return read_from(s.gsub!(/'\(/, '(quote ').gsub!(/([\(\)])/, ' \1 ').gsub!(/^[ \t]+|[ \t]+$/, '').split(/[ \t]+/))
	end

	def read_from(s)
		token = s.shift

		if token == '('
			l = []
			while s[0] != ')'
				l << read_from(s)
			end
			s.shift
			return l
		elsif token.start_with?('#\'')
			return ['fn-hash'.intern, token[2..-1].intern]
		else
			return numeric?(token) ? Float(token) : (token.start_with?('"') ? token[1..-2] : token.intern)
		end
	end

	def numeric?(t)
		return true if t =~ /^\d+$/
		true if Float(t) rescue false
	end
end

