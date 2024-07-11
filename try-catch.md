## 서론
UserDao 리펙토링 실습을 하기 이전에 next step 책의 설명을보는데  "expert one-on-one J2EE 설계와 개발"에서 제시한 컴파일타임 Exception과 런타임 Exception의 가이드라인이 인용된 부분을 읽었다.
- API를 사용하는 모든 곳에서 이 예외를 처리해야하는가? 예외가 반드시 메서드에 대한 반환 값이 되어야 하는가? 이 질문에 대한 답이 "예"일 경우 컴파일 타임 Exception을 사용해 컴파일러의 도움을 받는다
- API를 사용하는 소수 중 이 예외를 처리해야 하는가? 이 질문에 대한 답이 "예"일 경우 런타임 Exception으로 구현한다 API를 사용하는 모든 코드가 Exception을 catch하도록 강제하지 않는 것이 좋다
- 무엇인가 큰 문제가 발생했는가? 아 문제를 복구할 방법이 없는가? 이 질문에 대한 답이 "예"라면 런타임 Exception으로 구현한다. API를 사용하는 코드에서 Exception을 catch하더라도 에러에 대한 정보를 통보 받는 것 외에는 아무것도 할 수 있는 것이 없다
- 아직도 불명확한가? 그렇다면 런타임 Exception으로 구현하라 Exception에 대해 문서화 하고 API를 사용하는 곳에서 Exception에 대한 처리를 결정하도록 하라

한번 쯤 생각해볼 가이드 라인인 것 같아 적어보았다 사실 위 가이드 라인을 고려하기 전에 try catch문을 처음 배울 때는 if else문이 더 익숙한 상태이기 때문에 그냥 if else문으로 상태를 검사해 예외를 처리했었다. 하지만 프로젝트를 하다보면 런타임 예외처리에 대한 유연한 처리가 가능하다는 점과
에러 로그를 보기 좋다는 점을 깨닫게 되어 try catch문을 더 애용하게 되었다. 

예외처리 구문을 사용하면 어떤점이 좋은지 알아보고 에러와 exception에 대해서 알아보자

## 예외 처리의 장점
1. 예외 처리 코드와 일반 코드의 분리
소스코드에서 비정상적인 상황이 발생했을 때 예외 처리를 사용하지 않는다면 예외 조건 검사와 그에 대한 처리가 복잡한 코드로 구현되는 경우가 있다
파일을 메모리로 읽어들이는 상황을 생각해보자(수도코드)
```
readFile {
    open the file;
    determine its size;
    allocate that much memory;
    read the file into memory;
    close the file;
}
```
위 메서드를 실행할 때 발생할 수 있는 에러의 종류는 아래와 같다
- 파일을 열 수 없는 상황
- 파일의 길이를 확인할 수 없는 상황
- 충분한 메모리가 할당될 수 없는 상황
- 파일 읽기에 실패한 상황
- 파일이 닫힐 수 없는 상황
이런 경우에 readFile의 본래 핵심 로직에 비해 에러처리하는데 필요한 코드가 더 많아진다
```
errorCodeType readFile{
  initialize errorCode = 0;
  open the file;
  if(theFileIsOpen){
    determine the length of the file;
    if(gotTheFileLength){
      allocate that much memory;
      if(gotEnoughMemory){
        read the file into memory;
        if(readFailed{
          errorCode = -1;
        }
      } else{
          errorCode = -3;
      } close the file;
        if (theFileDidntClose && errorCode == 0) {
            errorCode = -4;
        } else {
            errorCode = errorCode and -4;
        }
    } else {
        errorCode = -5;
    }
    return errorCode;
}
```
뿐만 아니라 코드의 논리를 파악하기가 힘들어지는데 예외처리를 사용한다면 훨씬 파악하기 쉬운 코드가 된다
```
readFile {
    try {
        open the file;
        determine its size;
        allocate that much memory;
        read the file into memory;
        close the file;
    } catch (fileOpenFailed) {
       doSomething;
    } catch (sizeDeterminationFailed) {
        doSomething;
    } catch (memoryAllocationFailed) {
        doSomething;
    } catch (readFailed) {
        doSomething;
    } catch (fileCloseFailed) {
        doSomething;
    }
}
```
그리고 개발자가 생각치 못한 예외 상황도 처리할 수 있기 때문에 안정적이다
2. 호출 스택 상단으로 에러 전달
아래와 같이 readFile이 네번째로 호출되는 메서드라고 가정해보자
```
method1 {
    call method2;
}

method2 {
    call method3;
}

method3 {
    call readFile;
}
```
method1에서 readFile에서 발생하는 에러를 처리할 수 있으면 reaFile에서 에러 발생 시 호출 스택을 거꾸로 탐색하여 method3,method2가 method1에 
예외를 throws 할 수 있다
3. 에러 유형 그룹화 및 구분
구체적인 유형의 예외와 일반적인 방식으로 예외를 처리할 수 있다 대표적으로 IOException 같은 경우 FileNotFoundException, EOFException 등 모든 I/O 
예외를 처리할 수 있는데 예외의 구체적인 상황을 명시해주고 싶으면 자식 예외인 FileNotFoundException, EOFException을 사용하면 된다.

 
