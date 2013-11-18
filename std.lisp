(begin
	(define '- sub)
	(define '/ div)
	(define '% mod)
	(define '** pow)

	(define 'int floor)
	(define 'nil '())
	
	(define '== eq)
	(define '= eq)
	(define '!= ne)
	(define '< lt)
	(define '> gt)
	(define '<= le)
	(define '>= ge)

	(define
		'positive?
		(lambda
			(x)
			(> x 0)))
	(define
		'negative?
		(lambda
			(x)
			(< x 0)))
	(define
		'zero?
		(lambda
			(x)
			(== x 0)))
	(define
		'even?
		(lambda
			(x)
			(== 0 (mod x 2))))
	(define
		'odd?
		(lambda
			(x)
			(== 1 (mod x 2))))
	(define
		'int?
		(lambda
			(i)
			(== i (floor i))))

	(define
		'inc
		(lambda
			(x)
			(+ x 1)))
	(define
		'dec
		(lambda
			(x)
			(- x 1)))
	(define
		'round-to
			(lambda
				(x i)
				(let
					((s (** 10 (ceil (abs i)))))
					(/ (round (* x s)) s))))
	
	(define
		'join
		(lambda
			(fn l)
			(if
				(== (length l) 1)
				(car l)
				(fn
					(car l)
					(join fn (cdr l))))))
	
	(define
		'neg
		(lambda
			(x)
			(- 0 x)))
	(define
		'+
		(lambda
			(*l)
			(if
				(list? (car l))
				(if
					(empty? (cdr l))
					(car l)
					(+
						(cons
							(apply #'add (car l) (car (cdr l)))
							(cdr (cdr l)))))
				(join #'add l))))
	(define
		'*
		(lambda
			(*l)
			(join #'mul l)))

	(define
		'abs
		(lambda
			(x)
			(if
				(>= x 0)
				x
				(neg x))))
	(define
		'max
		(lambda
			(*l)
			(join
				(lambda
					(a b)
					(if
						(> a b)
						a
						b))
				l)))
	(define
		'min
		(lambda
			(*l)
			(join
				(lambda
					(a b)
					(if
						(< a b)
						a
						b))
				l)))
	(define
		'reverse
		(lambda
			(l)
			(if
				(empty? l)
				l
				(append
					(car l)
					(reverse (cdr l))))))
	(define
		'square
		(lambda
			(x)
			(* x x)))
	(define
		'sqrt
		(lambda
			(x)
			(** x 0.5)))
	(define
		'faculty
		(lambda
			(n)
			(if
				(<= n 1)
				1
				(* n (faculty (- n 1))))))
	(define
		'bin-co
		(lambda
			(n k)
			(/
				(faculty n)
				(*
					(faculty k)
					(faculty (- n k))))))
	(define
		'accu
		(lambda
			(k n ev)
			(if
				(> k n)
				0
				(+
					(ev k)
					(accu (+ k 1) n ev)))))
	(define
		'map
		(lambda
			(fn l)
			(if
				(empty? l) 
				l
				(cons
					(fn (car l))
					(map fn (cdr l))))))
	(define
		'array-elem
		(lambda
			(n l)
			(cond
				((not (list? l))	nil)
				((empty? l)			nil)
				((zero? n)			(car l))
				(t					(array-elem
										(- n 1)
										(cdr l))))))
	(define 'nth #'array-elem)
	(define 'array-set
		(lambda
			(n l v)
			(if
				(zero? n)
				(cons v (cdr l))
				(cons (car l) (array-set (- n 1) (cdr l) v)))))
	(define
		'merge
		(lambda
			(x y)
			(if
				(empty? x)
				y
				(cons
					(car x)
					(merge (cdr x) y)))))
	(define
		'flatten
		(lambda
			(l)
			(cond
				((empty? l)			l)
				((list? (car l))	(flatten (merge (car l) (cdr l))))
				(t					(cons (car l) (flatten (cdr l)))))))
	(define
		'member?
		(lambda
			(e l)
			(cond
				((not (list? l))	nil)
				((empty? l) 		f)
				((== (car l) e)		t)
				(t					(member? e (cdr l))))))
	(define
		'make-set
		(lambda
			(*l)
			(let
				((settify	(lambda
								(l)
								(cond
									((empty? l)					nil)
									((member? (car l) (cdr l))	(settify (cdr l)))
									(t							(cons (car l) (settify (cdr l))))))))
				(settify (flatten l)))))
	(define
		'union
		(lambda
			(l1 l2)
			(make-set (merge l1 l2))))
	(define
		'intersect
		(lambda
			(l1 l2)
			(let
				((int-impl	(lambda
				 				(l1 l2)
								(cond
									((empty? l2)			nil)
									((member? (car l2) l1)	(cons
																(car l2)
																(int-impl l1 (cdr l2))))
									(t						(int-impl l1 (cdr l2)))))))
				(int-impl
					(flatten l1)
					(flatten l2)))))
	(define
		'difference
		(lambda
			(l1 l2)
			(let
				 ((dif-impl	(lambda
								(l1 l2)
								(cond
									((empty? l1)			nil)
									((member? (car l1) l2)	(dif-impl (cdr l1) l2))
									(t						(cons
																(car l1)
																(dif-impl (cdr l1) l2)))))))
				(dif-impl (flatten l1) (flatten l2)))))
	(define
		'repeat
		(lambda
			(n fn v)
			(if
				(zero? n)
				v
				(repeat (- n 1) fn (fn v)))))
	(define
		'find-first-if
		(lambda
			(fn l)
			(cond
				((empty? l)		nil)
				((fn (car l))	(car l))
				(t				(find-first-if fn (cdr l))))))
	(define
		'find-all-if
		(lambda
			(fn l)
			(cond
				((empty? l)		nil)
				((fn (car l))	(cons
									(car l)
									(find-all-if fn (cdr l))))
				(t				(find-all-if fn (cdr l))))))
	(define
		'iterate
		(lambda
			(i fnC fnB fnI)
			(if
				(fnC i)
				(begin
					(fnB i)
					(iterate (fnI i) fnC fnB fnI))
				nil)))
	(define
		'for
		(lambda
			(i n fn)
			(iterate
				i
				(lambda (i) (!= i n))
				fn
				(lambda (i) (+ i (if (< i n) 1 -1))))))
	(define
		'for-inclusive
		(lambda
			(i n fn)
			(let
				((m (if (< i n) (+ n 1) (- n 1))))
				(for i m fn))))
	(define
		'apply
		(lambda
			(fn *l)
			(if
				(empty? l)
				nil
				(if
					(list? (car l))
					(if
						(empty? (car l))
						nil
						(cons
							(eval (cons fn (map #'car l)))
							(apply fn (map #'cdr l))))
					(fn l)))))
	(define 'vector-add #'+)
	(define 'apply-vector #'+)
	(define
		'vector-scale
		(lambda
			(s v)
			(map (lambda (d) (* s d)) v)))
	(define
		'vector-length
		(lambda
			(v)
			(sqrt (+ (map #'square v)))))
	(define
		'vector-dot
		(lambda
			(v1 v2)
			(+ (apply #'mul v1 v2))))
	(define
		'vector-cross
		(lambda
			(v1 v2)
			(list
				(- (* (nth 1 v1) (nth 2 v2)) (* (nth 2 v1) (nth 1 v2)))
				(- (* (nth 2 v1) (nth 0 v2)) (* (nth 0 v1) (nth 2 v2)))
				(- (* (nth 0 v1) (nth 1 v2)) (* (nth 1 v1) (nth 0 v2))))))
	(define
		'execute-over-1d
		(lambda
			(fn i n)
			(for-inclusive i n fn)))
	(define
		'execute-over-2d
		(lambda
			(fn c1 c2)
			(for-inclusive
				(car c1)
				(car c2)
				(lambda
					(i)
					(execute-over-1d
						(lambda
							(j)
							(fn i j))
						(nth 1 c1)
						(nth 1 c2))))))
	(define
		'execute-over-3d
		(lambda
			(fn c1 c2)
			(for-inclusive
				(car c1)
				(car c2)
				(lambda
					(i)
					(execute-over-2d
						(lambda
							(j k)
							(fn i j k))
						(cdr c1)
						(cdr c2))))))
	(define
		'compare
		(lambda
			(fn l1 l2)
			(if
				(empty? l1)
				nil
				(cons
					(fn (car l1) (car l2))
					(compare fn (cdr l1) (cdr l2))))))
	(define
		'sort-coords
		(lambda
			(c1 c2)
			(list
				(compare #'min c1 c2)
				(compare #'max c1 c2))))
	(define
		'collect-data-1d
		(lambda
			(fn i n)
			(cond
				((> i n)	(collect-data-1d fn n i))
				((== i n)	(list (fn i)))
				(t			(cons (fn i) (collect-data-1d fn (+ i 1) n))))))
	(define
		'collect-data-2d
		(lambda
			(fn c1 c2)
			(cond
				((> (car c1) (car c2))	(collect-data-2d fn (cons (car c2) (cdr c1)) (cons (car c1) (cdr c2))))
				((== (car c1) (car c2))	(list 
											(collect-data-1d
												(lambda
													(i)
													(fn (car c1) i))
												(nth 1 c1)
												(nth 1 c2))))
				(t						(cons
											(collect-data-1d
												(lambda
													(i)
													(fn (car c1) i))
												(nth 1 c1)
												(nth 1 c2))
											(collect-data-2d fn (cons (+ (car c1) 1) (cdr c1)) c2))))))
	(define
		'collect-data-3d
		(lambda
			(fn c1 c2)
			(cond
				((> (car c1) (car c2))	(collect-data-3d fn (cons (car c2) (cdr c1)) (cons (car c1) (cdr c2))))
				((== (car c1) (car c2))	(list
											(collect-data-2d
												(lambda
													(y z)
													(fn (car c1) y z))
												(cdr c1)
												(cdr c2))))
				(t						(cons
											(collect-data-2d
												(lambda
													(y z)
													(fn (car c1) y z))
												(cdr c1)
												(cdr c2))
											(collect-data-3d fn (cons (+ (car c1) 1) (cdr c1)) c2))))))
	(define
		'gcd
		(lambda
			(a0 a1)
			(if
				(< a0 a1)
				(gcd a1 a0)
				(let
					((a2 (% a0 a1)))
					(if
						(zero? a2)
						a1
						(gcd a1 a2))))))
	(define
		'lcm
		(lambda
			(a0 a1)
			(/
				(* a0 a1)
				(gcd a0 a1))))
	(define
		'prime?
		(lambda
			(p)
			(cond
				((not (int? p))	f)
				((<= p 1)		f)
				(t				(let
									((dfn (lambda
											(p n)
											(cond
												((<= n 1) 			t)
												((== p n)			t)
												((zero? (% p n))	f)
												(t					(dfn p (- n 1))))))
									 (n (ceil (sqrt p))))
									(dfn p n))))))
	(define
		'make-list
		(lambda
			(a b)
			(cond
				((not (and (int? a) (int? b)))	nil)
				((> a b)						(make-list b a))
				((== a b)						(list a))
				(t								(cons a (make-list (+ a 1) b))))))
	(define
		'rand-int
		(lambda
			(a b)
			(if
				(< a b)
				(rand-int b a)
				(+ a (round (* (rand) (- b a)))))))
	(define
		'heron
		(lambda
			(a n)
			(if
				(== n 1)
				1
				(let
					((h (heron a (- n 1))))
					(/
						(+ h (/ a h))
						2)))))
	(define 
		'horner 
		(lambda 
			(x p) 
			(if 
				(empty? p) 
				0
				(+ 
					(car p) 
					(* 
						x 
						(horner x (cdr p)))))))
)

