2. 인덱스 기본

2.1 인덱스 구조 및 탐색
- 데이터베이스 테이블에서 데이터를 찾는 방법: 테이블 전체를 스캔/인덱스를 이용
- 인덱스 튜닝의 두가지 핵심요소
  - 큰 테이블에서 소량 데이터를 검색할 때 사용
  - 온라인 트렌젝션 처리(Online Transaction Processing, OLTP) 소량 데이터 주로 검색 > 인덱스 필수
  1) 인덱스 스캔 효율화 튜닝: 인덱스 스캔 과정에서 발생하는 비효율을 줄임
  2) 랜덤 액세스 최소화 튜닝: 테이블 액세스 횟수 줄임
    - 인덱스 스캔 후 테이블 레코드 액세스 시 랜덤 I/O(디스크I/O 중 하나) 방식 사용 > 랜덤 I/O 가 속도에 제일 큰 영향
    - sql 튜닝에 1) 보다 더 큰 영향
- 인덱스 구조
  - 인덱스: 대용량 테이블에서 필요한 데이터만 빠르게 효율적으로 액세스하기 위해 사용하는 오브젝트
    - 범위 스캔 가능하게 만듬 > 인덱스가 정렬되어 있기 때문
  - B*Tree 인덱스 (Balanced)
    - balanced: 어떤 값으로 탐색하더라도 인덱스 루트에서 리프 블록에 도달하기까지 읽는 블록 수가 같음
    - 루트와 브랜치블록에 있는 각 레코드는 하위 블록에 대한 주소값을 갖음
    - LMC(Leftmost Child) : 가장 왼쪽의 첫번째 레코드
      자식 노드 중 가장 왼쪽 끝에 위치한 블록을 가리킴
    - Leaf block: ROWID(테이블 레코드를 가리키는 주소값)을 갖음
  - ROWID = 데이터 블록 주소(데이터 파일번호 + 블록번호(데이터파일 내에서 부여한 상대적 순번)) + 로우번호(블록 내 순번)
    - 이 값을 알면 테이블 레코드를 찾아감
- 인덱스 탐색과정
  1) 수직적 탐색: 인덱스 스캔 시작지점을 찾는 과정
    - root > branch > leaf
    - 찾고자 하는 값보다 크거나 같은값을 만나면 바로 직전 레코드가 가리키는 하위 블록으로 이동
    - 조건에 맞는 첫번째 레코드를 찾는 과정
  2) 수평적 탐색: 데이터를 찾는 과정
    - 양뱡향 연결 리스트(double linked list) 구조: 인덱스 리프 블록끼리는 서로 앞뒤 블록에 대한 주소값을 갖음
    - 수평적으로 탐색하는 이유
      1) 조건을 만족하는 데이터 모두 찾기 위해
      2) ROWID를 얻기 위해 > 테이블 액세스 하기 위해 필요하므로
- 결합 인덱스 구조와 탐색
  - 두개 이상 컬럼을 결합해서 인덱스 생성 가능 
  - 인덱스를 어떻게 구성하든 블록 I/O개수가 같으면 성능도 같다


2.2 인덱스 기본 사용법
- 인덱스 Range Scan 하는 방법이 중요(Full Scan시 리프 블록 전체를 스캔해야함)
  1) 인덱스 컬럼을 가공시 : 인덱스 스캔 시작점을 못찾음 (컬럼 가공(substr, nvl)/OR,IN조건/%LIKE%)
  2) 인덱스 선두 컬럼이 쿼리 조건절에 있어야 함
  - **인덱스 선두 컬럼이 가공되지 않은 상태로 조건절에 있어야 Range Scan 가능**
  - OR Expansion: Or 조건식을 Index Range Scan 을 적용하기 위해 쿼리변환 하는 것
  - IN-List Iterator: In 조건절에 옵티마이저가 사용하는 방식 
    - IN 조건 개수만큼 Index Range Scan을 반복한다 > Union All 한것과 같은 결과 방식
- 인덱스를 이용한 소트 연산 생략
  - 인덱스는 테이블과 달리 컬럼 별 정렬되어 있다. 
  - 인덱스 & 조건절을 잘 쓰면 order by 절이 있어도 옵티마이저에선 수행하지 않음
  - default가 아닌 desc정렬도 RANGE SCAN DESCENDING을 쓰면 sort를 쓰지 않음 > 블록은 양방향 연결이므로!!
- ORDER BY 절에서 컬럼 가공
  - 조건절이 아닌 orderby/select-list에서 컬럼을 가공하면 인덱스를 정상적으로 사용 불가
  - ORDER BY A || B : 정렬연산 생략 불가
  - Select TO_CHAR(A) from B Order By A : 가공한 A값으로 정렬을 요청하므로 sort 생략 불가
    > 이럴 땐 B.A로 명시해 주면 가공안된 B의 A값으로 sort 하므로 생략 가능
    > ORDER BY 1 : SELECT 첫 컬럼이라는 뜻 > 이것도 가공됐으면 sort 생략 불가
- SELECT-LIST에서 컬럼 가공
  - MIN/MAX 도 인덱스가 잘 적용되어 있으면 정렬 연산을 따로 수행하지 X
    > FIRST ROW 맨왼쪽/맨오른쪽 만 수행하면 얻을 수 있는 값
  - SELECT NVL(MAX(TO_NUMBER(A)), 0) vs. NVL(TO_NUMBER(MAX(A)), 0)
  - 스칼라 서브쿼리 : SELECT에서 서브쿼리 > 잘하면 SORT 연산 X but 쿼리가 복잡하다
- 자동 형변환
  - 오라클 : 조건절에서 양쪽 값의 데이터 타입이 달라도 자동 형변환 처리
    - 문자형 = 숫자형 > 숫자형 컬럼 기준으로 문자형 컬럼을 변환함
    - 날짜형 = 문자형 > 날짜형 컬럼 기준으로 문자형 컬럼을 변환 
      > 근데 좌변 컬럼이 형변환 되는게 아니면 인덱스 성능 문제 없음!! 
  - LIKE 연산자
    - Like 자체가 문자열 비교 연산자 > 숫자형이 문자형으로 변환
    - LIKE :bind || '' > 이미 효율이 안좋음 + bind변수가 숫자형이면 더 나빠짐
  - decode(a,b,c,d): a=b?c:d > c가 null 이면 d는 varchar2로 취급. > to_number(c)로 해야 숫자인 d와 비교 수월


2-3 인덱스 확장기능 사용법
1) Index Range Scan
  - B*Tree 인덱스의 일반적이고 정상적인 형태의 액세스 방식
  - 인덱스 루트에서 리프 블록까지 수직적으로 탐색한 후에 필요한 범위만 스캔
  - 선두 컬럼을 가공하지 않은 상태로 조건절에 사용
  - 성능: 인덱스 스캔범위, 테이블 액세스 횟수를 줄여야 향상
  - INDEX (RANGE SCAN)
2) Index Full Scan
  - 수직적 탐색 없이 인덱스 리프 블록을 처음부터 끝까지 수평적으로 탐색
  - 데이터 검색을 위한 최적의 인덱스가 없을 때 차선으로 사용
  - 인덱스 선두 컬럼이 조건절에 없지만 두번째 컬럼에 있으므로 full scan 가능
  - 면적이 큰 테이블보다 인덱스를 스캔하는 쪽이 유리할 때 사용
  - Range Scan과 마찬가지로 인덱스 컬럼 순으로 정렬 > sort order by 연산 생략 가능
  - INDEX (FULL SCAN)
3) Index Unique Scan
  - 수직적 탐색만으로 데이터를 찾는 스캔방식
  - Unique 인덱스를 = 조건으로 탐색하는 경우
  - 부분범위 처리가 가능할 때
  - RANGE SCAN으로 변환되는 경우
    1) Unique 인덱스여도 범위검색(between, 부등호, like) 조건일 때 
    2) Unique 결합 인덱스(주문일자+고객ID+상품ID)인데 일부 컬럼(주문일자+고객ID)만 검색할 때
  - Index (UNIQUE SCAN)
4) Index Skip Scan(9i)
  - 조건절에 빠진 인덱스 선두 컬럼의 Distinct Value 개수가 적고 후행 컬럼의 Distinct Value개수가 많을 때 유용
    > ex. 고객 테이블에서 Distinct Value 개수가 가장 적은 컬럼 : 성별 / 가장 많음 : 고객번호
  - 루트 또는 브랜치 블록에서 읽은 컬럼 값 정보를 이용해 조건절에 부합하는 레코드를 포함할 가능성이 있는 리프블록만 골라서 액세스
  - 작동하기 위한 조건 : Index Range Scan이 불가능하거나 효율적이지 못한 상황
    1) 선두 컬럼에 대한 조건절은 있고 중간 컬럼에 대한 조건절이 없는 경우
    2) Distinct Value가 적은 두 개의 선두컬럼이 모두 조건절에 없는 경우
    3) 선두컬럼이 범위검색 조건일 때
  - index_ss, no_index_ss 힌트: 이 스캔방식을 유도/방지
  - **하지만 인덱스는 기본적으로 최적의 Index Range Scan을 목표로 설계해야함.** 
  - INDEX (SKIP SCAN)
5) Index Fast Full Scan
  1) 인덱스 세그먼트 전체 스캔 > Index Full Scan 보다 빠른 이유: Index Full Scan 같은 논리적인 인덱스 트리구조 무시  
  2) 결과집합 순서 보장 안됨 : 물리적으로 디스크에 저장된 순서대로 읽음 > 인덱스 키 순서대로 정렬되지 X
  3) Multiblock I/O방식 : 디스크에서 대량의 인덱스 블록을 읽어야할 때 큰 효과
  4) 인덱스가 파티션 돼 있지 않아도 병렬 쿼리 가능(파티션 되어 있어야 Index Range Scan, Index Full Scan은 는 병렬쿼리 가능)
    > 병렬 쿼리 시 Direct Path I/O 방식 사용하므로 I/O 속도가 더 빠름
  5) 쿼리에 사용한 컬럼이 모두 인덱스에 포함돼 있을 때만 사용 가능
  - index_ffs, no_index_ffs 힌트
6) Index Range Scan Descending
  - Index Range Scan과 기본적으로 동일한 스캔 방식
  - 인덱스를 뒤에서부터 앞쪽으로 스캔 > 내림차순으로 정렬된 결과집합 생성
  - index_desc 힌트
  - INDEX (RANGE SCAN DESCENDING)
  