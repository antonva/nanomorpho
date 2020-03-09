"fizzbuzz.mexe" = main in
!
{{
#"fizzbuzz[f1]" =
[
(MakeVal null)
(Push)
(Push)
(MakeVal 0)
(Store 1)
_0:
(Fetch 1)
(Push)
(MakeVal 101)
(Push)
(Call #"<[f2]" 2)
(GoFalse _1)
(Fetch 1)
(Push)
(MakeVal 15)
(Push)
(Call #"%[f2]" 2)
(Push)
(MakeVal 0)
(Push)
(Call #"==[f2]" 2)
(GoFalse _2)
(MakeVal "fizzbuzz")
(Push)
(Call #"writeln[f1]" 1)
(Go _3)
_2:
(Fetch 1)
(Push)
(MakeVal 3)
(Push)
(Call #"%[f2]" 2)
(Push)
(MakeVal 0)
(Push)
(Call #"==[f2]" 2)
(GoFalse _4)
(MakeVal "fizz")
(Push)
(Call #"writeln[f1]" 1)
(Go _5)
_4:
(Fetch 1)
(Push)
(MakeVal 5)
(Push)
(Call #"%[f2]" 2)
(Push)
(MakeVal 0)
(Push)
(Call #"==[f2]" 2)
(GoFalse _6)
(MakeVal "buzz")
(Push)
(Call #"writeln[f1]" 1)
(Go _7)
_6:
_7:
_5:
_3:
(Go _0)
_1:
(MakeVal 0)
(Return)
];
#"main[f0]" =
[
(MakeVal null)
(MakeVal 100)
(Push)
(Call #"fizzbuzz[f1]" 1)
];
}}
*
BASIS;
