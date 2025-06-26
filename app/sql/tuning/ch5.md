5. 소트 튜닝
PGA-Temp 테이블 스페이스 활용 : 소트머지조인, 해시조인, 데이터소트, 그루핑

5.1 소트 연산에 대한 이해
- 종류
    1) 메모리 소드(In-Memory Sort) : Internal Sort -> 메모리 내에서 완료
    2) 디스크 소트(To-Disk Sort) : External Sort -> 할당받은 Sort Area내에서 정렬을 완료하지 못해 디스크 공간까지 사용하는 경우
- Sort Run : Sort Area가 찰 때마다 Temp영역에 저장한 중간단계 집합
- 메모리 집양적 + CPU집약적 : 처리량이 많으면 디스크 I/O 발생하므로 쿼리 성능 좌우
- 부분범위 처리를 불가능하게 함으로써 OLTP환경에서 성능 저하시키는 주요인
- 소트를 발생시키는 오퍼레이션
    1) Sort Aggregate: 전체 로우를 대상으로 집계(실제로 데이터 정렬X, 변수사용: max, min, count, sum)
    2) Sort Order By: 데이터 정렬
    3) Sort Group By: 소팅 알고리즘+그룹별 집계 수행
        - order by 를 쓰지 않으면 논리적 정렬 결과 받지 못함.  
        - 1)과 비슷한 매커니즘, 집계할 대상이 아무리 많아도 Temp 쓰지 않음.
        - Hash Group By(10gR2): 해싱 알고리즘, Temp 쓰지 않음
    4) Sort Unique : Unnesting된 서브쿼리의 조인 컬럼에 Unique 인덱스가 없으면 사용
        - 집합(Set) 연산자 사용할 때 : Union, Minus, Intersect
        - Distinct 연산자 사용시(10gR2에는 Hash Unique 방식 사용)
    5) Sort Join : 소트 머지 조인 수행할 때
    6) Window Sort : 윈도우함수(분석함수)를 수행할 때


5.2 소트가 발생하지 않도록 SQL작성
- Union, Minus, Distinct 연산자는 중복 레코드를 제거하기 위한 소트연산 발생 -> 꼭 필요한 경우만 사용
- Union vs. Union all
    두 집합이 상호 배타적이면 굳이 Union이 아닌 Union all을 사용하면 소트연산 X 
    인스턴스 중복 가능성이 있다면 Union all을 사용하면서 <>(같지않음)으로 중복 방지
        Null 허용 컬럼이라면 1)<>이거나(or) is null 2)LNNVL(=)
- Exists 활용
    Distinct 대체(데이터 존재 여부만 확인하면 될 때) -> 부분범위 처리도 가능하게 함
    Minus 연산자는 Not Exists로 대체
- 조인방식 변경
    해시조인 + order by 사용하면 sort order by 가 나타남 
        -> use_nl로 소트연산 생략 가능 
        -> 정렬 기준이 조인 키 컬럼이면 소트머지 조인도 생략 가능


5.3 인덱스를 이용한 소트 연산 생략



5.4 Sort Area를 적게 사용하도록 SQL 작성
- 소트연산이 불가피하면 메모리 내에서 처리하도록 노력
- Sort Area를 최대한 적게 사용하는 방향으로
- Top N 쿼리의 소트 부하 경감 원리
    Top N쿼리 + 소트연산 생략가능 인덱스 구성 효과(Top N Stopkey 알고리즘) 좋음
    but, 인덱스로 소트연산을 생략할 수 없을 때 -> Top N 소트 알고리즘 사용
        :처음 데이터 N개를 order by 조건으로 정렬하고 그 뒤에 읽는 데이터를 끝 값부터 비교해서 교체해나감
- Top N 쿼리가 아닐 때 발생하는 소트부하 : Temp 테이블 스페이스 사용(디스크)
- 분석함수에서의 Top N 소트: 윈도우 함수 중 rank/row_number함수는 max함수보다 소트 부하가 작다
    ->Top N 소트 알고리즘이 작동하기 때문