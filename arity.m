"arity.mexe" = main in
!
{{
#"arity[f1]" =
[
(MakeVal null)
(Push)
(Fetch 0)
(Store 1)
(Return)
];
#"main[f0]" =
[
(MakeVal null)
(Call #"arity[f1]" 0)
];
}}
*
BASIS;
