--테스트용 데이터
CREATE TABLE simple_test (
                             id NUMBER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
                             name VARCHAR2(100)
);

INSERT INTO simple_test (name) VALUES ('테스트데이터1');
select * from simple_test;
