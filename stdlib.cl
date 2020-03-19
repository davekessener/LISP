(define defun (macro (name args body) `(define ,name (lambda ,args ,body))))
(define defmacro (macro (name args body) `(define ,name (macro ,args ,body))))

(defun printf (fmt . a) (write *STDIO* (format fmt . a)))

(defun nil? (l) (eq? l NIL))
(define empty? nil?)
(defun caar (l) (car (car l)))
(defun caaar (l) (caar (car l)))
(defun cddr (l) (cdr (cdr l)))
(defun cdddr (l) (cddr (cdr l)))
(defun cadr (l) (car (cdr l)))
(defun caddr (l) (car (cddr l)))
(defun cadddr (l) (car (cdddr l)))

(defun == (a b) (eq? a b))
(defun != (a b) (not (== a b)))
(defun < (a b) (negative? (- a b)))
(defun > (a b) (< b a))
(defun <= (a b) (not (> a b)))
(defun >= (a b) (not (< a b)))

(defun list l l)

(defmacro let (params body)
	(begin
		(defun impl (p)
			(if (nil? p)
				(cons body NIL)
				(cons
					`(define ,(car (car p)) ,(cadr (car p)))
					(impl (cdr p)))))
		(cons 'begin (impl params))))

(defmacro cond clauses
	(begin
		(defun impl (c)
			(if (empty? c)
				NIL
				(list 'if (car (car c))
					(cadr (car c))
					(impl (cdr c)))))
		(impl clauses)))

(defun str-split (s) (begin
	(defun impl (n i)
		(if (== n i)
			NIL
			(cons
				(ord s i)
				(impl n (+ i 1)))))
	(impl (str-len s) 0)))

(defun str-join (l) (begin
	(defun impl (s r)
		(if (empty? r)
			s
			(impl
				(str-append s (chr (car r)))
				(cdr r))))
	(impl "" l)))

(defun sqrt (v) (let [
	(impl (lambda (x)
		(begin
			(define y (* 0.5 (+ x (/ v x))))
			(if (eq? x y)
				x
				(impl y)))))
]
	(impl (* 0.5 v))))

(defun length (l)
	(if (empty? l)
		0
		(+ 1 (length (cdr l)))))

(defun cut (l c)
	(if (== c 0)
		NIL
		(cons (car l) (cut (cdr l) (- c 1)))))

(defun sublist (l from to)
	(if (== 0 from)
		(cut l to)
		(sublist (cdr l) (- from 1) (- to 1))))

(defun readline (prompt io)
	(begin
		(write io prompt)
		(defun getc NIL (car (read io 1)))
		(defun impl (c)
			(cond
				((== c '\r')
					(impl (getc)))
				((== c '\n')
					NIL)
				(#t
					(cons c (impl (getc))))))
		(str-join (impl (getc)))))

; ======================================================================================

(define *test-str* "Hello, World!")
(printf "%s (x%d)\n" *test-str* 100)
(printf "joining: %s\n" (str-join (str-split *test-str*)))
(printf "\"%s\" == %s\n" *test-str* (str-split *test-str*))
(printf "sqrt(1 / 2) == %.4f\n" (sqrt (/ 1 2)))
(printf "%s\n" (macro-expand (cond ((== c '\r') (impl (getc))) ((== c '\n') NIL) (#t (cons c (impl (getc)))))))
(let [
	(s (readline "> " *STDIO*))
	(l (str-len s))
]
	(cond
		((== l 1)
			(printf "ONE!\n"))
		((== l 2)
			(printf "TWO!\n"))
		((== l 3)
			(printf "THREE!\n"))
		((> l 5)
			(printf "more than FIVE!\n"))
		(#t
			(printf "default\n"))))
(printf "Goodbye.\n")

