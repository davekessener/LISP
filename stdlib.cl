(define defun (macro (name args body) `(define ,name (lambda ,args ,body))))
(define defmacro (macro (name args body) `(define ,name (macro ,args ,body))))

(defun printf (fmt . a) (write *STDIO* (format fmt . a)))

(defun nil? (l) (eq? l NIL))
(defun caar (l) (car (car l)))
(defun caaar (l) (caar (car l)))
(defun cddr (l) (cdr (cdr l)))
(defun cdddr (l) (cddr (cdr l)))
(defun cadr (l) (car (cdr l)))
(defun caddr (l) (car (cddr l)))
(defun cadddr (l) (car (cdddr l)))
(defun == (a b) (eq? a b))

(defmacro let (params body)
	(begin
		(defun impl (p)
			(if (nil? p)
				(cons body NIL)
				(cons
					`(define ,(car (car p)) ,(cadr (car p)))
					(impl (cdr p)))))
		(cons 'begin (impl params))))

(defun str-split (s) (let [
	(impl (lambda (n i)
		(if (== n i)
			NIL
			(cons
				(ord s i)
				(impl n (+ i 1))))))
]
	(impl (str-len s) 0)))

(defun sqrt (v) (let [
	(impl (lambda (x)
		(begin
			(define y (* 0.5 (+ x (/ v x))))
			(if (eq? x y)
				x
				(impl y)))))
]
	(impl (* 0.5 v))))

; ======================================================================================

(define *test-str* "Hello, World!")
(printf "%s (x%d)\n" *test-str* 100)
(printf "sqrt(1 / 2) == %.4f\n" (sqrt (/ 1 2)))
(printf "\"%s\" == %s\n" *test-str* (str-split *test-str*))
(printf "Goodbye.\n")

