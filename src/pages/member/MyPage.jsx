import React, { useEffect, useState } from 'react';
import { Button, Card, Container, Form, Pagination, Tab, Tabs } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const MyPage = () => {

    const navi = useNavigate();
    const[maxPage, setMaxPage] = useState(1);
    const[page, setPage] = useState(1);
    const[pList, setPList] = useState([]);
    const[name, setName] = useState('');
    const[myinfo, setMyinfo] = useState({
        'name': '',
        'email': ''
    });

    useEffect(() => {

        myInfoUpdate();
        //myProductList();

    }, []);

    // 만료된 accessToken을 새로 발급하기
    const newAccessToken = async() => {
        console.log("신규 accessToken 생성");

        try {
            const res = await fetch(`http://localhost:8081/member/refresh-token`, {
                method: "POST",
                headers: {
                    "Content-type": "application/json;charset=utf-8"
                },
                credentials: "include",
                body: JSON.stringify({'refreshToken': localStorage.getItem("refreshToken")})
            });

            if(!res.ok) {
                alert("인증 오류 발생");
            } else {
                const data = await res.json();
                localStorage.setItem("accessToken", data.accessToken);
            }

        } catch {
            alert("인증 오류");
        }
    }

    // 내가 등록한 상품 목록 가져오기
    const myProductList = () => {

        fetch(`http://localhost:8081/product/myPList`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
            },
            credentials: "include"
        })
        .then((res) => {

            if(res.ok) {
                return res.json();
            } else if(res.status === 401) {
                if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                    newAccessToken();
                } else {

                    alert("로그아웃 되셨습니다.");

                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");

                    navi("/");
                }
            } else {
                return null;
            }
        })
        .then((res) => {

            if(res !== null) {

                setPList({
                    ...pList,
                    ...res
                });

                setMaxPage(res.length);
            }
        })
        .catch((err) => alert("에러 발생", err));

    }

    // 내 정보 가져오기
    const myInfoUpdate = () => {

        fetch(`http://localhost:8081/member/mypage/myinfo`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
            },
            credentials: "include"
        })
        .then((res) => {

            if(res.ok) {
                return res.json();
            } else if(res.status === 401) {
                if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                    newAccessToken();
                } else {

                    alert("로그아웃 되셨습니다.");

                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");

                    navi("/");
                }
            } else {
                return null;
            }
        })
        .then((res) => {

            if(res !== null) {

                setMyinfo({
                    ...myinfo,
                    ...res
                });
            }
        })
        .catch((err) => alert("에러 발생", err));
    }

    // user 이름 바꾸기
    const nameChange = async(e) => {

        e.preventDefault();

        try {
            const res = await fetch(`http://localhost:8081/member/mypage/myname`, {
                method: "POST",
                headers: {
                    "Content-type": "application/json;charset=utf-8",
                    "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
                },
                credentials: "include",
                body: JSON.stringify({'name': name})
            });

            if(res.ok) {
                const data = await res.json();

                setMyinfo({ ...myinfo, 'name': name });

                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);

                myInfoUpdate();

            } else if(res.status === 401) {
                if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                    newAccessToken();
                } else {

                    alert("로그아웃 되셨습니다.");

                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");

                    navi("/");
                }

            } else {
                alert("통신 에러");
            }


        } catch {
            console.log("통신 실패");
        }
    }

    return (
        <div>
            <br />
            <br />
            <Container>
                <Card>
                    <Card.Body>
                        <Tabs
                            defaultActiveKey="home"
                            id="fill-tab-example"
                            className="mb-3"
                            justify
                        >
                            <Tab eventKey="home" title="내 정보">
                                <Card.Text>이름 : {myinfo.name}</Card.Text>
                                <Card.Text>이메일 : {myinfo.email}</Card.Text>
                            </Tab>


                            <Tab eventKey="name" title="이름 수정">
                                <Form onSubmit={nameChange}>
                                    <Form.Group className="mb-3" controlId="formBasicEmail">
                                        <Form.Label>이름</Form.Label>
                                        <Form.Control
                                            type="text"
                                            placeholder="이름을 입력해 주세요."
                                            onChange={(e) => setName(e.target.value)}
                                            required
                                        />
                                    </Form.Group>

                                    <Button variant="primary" type="submit">변경</Button>
                                    {' '}
                                    <Button variant="danger" type="reset">초기화</Button>
                                </Form>
                            </Tab>


                            <Tab eventKey="product" title="등록한 상품">
                                
                                <Pagination>
                                    {page > 2 ? <Pagination.First /> : <Pagination.First disabled/>}
                                    {page !== 1 ? <Pagination.Prev /> : <Pagination.Prev disabled/>}
                                    {page-2 > 0 && <Pagination.Item onClick={(e) => setPage(e.target.value)}>{page-2}</Pagination.Item>}
                                    {page-1 > 0 && <Pagination.Item onClick={(e) => setPage(e.target.value)}>{page-1}</Pagination.Item>}


                                    <Pagination.Item active>{page}</Pagination.Item>


                                    {/* 추후 마지막 페이지 값 변경할 것 */}
                                    {page+1 < maxPage && <Pagination.Item onClick={(e) => setPage(e.target.value)}>{page+1}</Pagination.Item>}
                                    {page+2 < maxPage && <Pagination.Item onClick={(e) => setPage(e.target.value)}>{page+2}</Pagination.Item>}
                                    {page !== maxPage && <Pagination.Next />}
                                    {page < maxPage-1 && <Pagination.Last />}
                                </Pagination>
                            </Tab>


                            <Tab eventKey="order" title="주문한 상품">
                                
                                <Pagination>
                                    {page > 2 ? <Pagination.First /> : <Pagination.First disabled/>}
                                    {page!==1 ? <Pagination.Prev /> : <Pagination.Prev disabled/>}
                                    {page-2>0 && <Pagination.Item onClick={(e) => setPage(e.target.value)}>{page-2}</Pagination.Item>}
                                    {page-1>0 && <Pagination.Item onClick={(e) => setPage(e.target.value)}>{page-1}</Pagination.Item>}


                                    <Pagination.Item active>{page}</Pagination.Item>


                                    {/* 추후 마지막 페이지 값 변경할 것 */}
                                    {page+1<10 && <Pagination.Item onClick={(e) => setPage(e.target.value)}>{page+1}</Pagination.Item>}
                                    {page+2<10 && <Pagination.Item onClick={(e) => setPage(e.target.value)}>{page+2}</Pagination.Item>}
                                    {page!==10 ? <Pagination.Next /> : <Pagination.Next disabled/>}
                                    {page < 9 ? <Pagination.Last /> : <Pagination.Next disabled/>}
                                </Pagination>
                            </Tab>
                        </Tabs>






                        {/*
                        <Pagination>
                            {page!=1 && 
                            
                                <>
                                    <Pagination.First />
                                    <Pagination.Prev />
                                </>
                            }
                            <Pagination.Item>{1}</Pagination.Item>
                            <Pagination.Ellipsis />


                            <Pagination.Item>{10}</Pagination.Item>
                            <Pagination.Item>{11}</Pagination.Item>

                            <Pagination.Item active>{12}</Pagination.Item>

                            <Pagination.Item>{13}</Pagination.Item>
                            <Pagination.Item>{14}</Pagination.Item>


                            <Pagination.Ellipsis />
                            <Pagination.Item>{20}</Pagination.Item>

                            {/* 추후 값 변경할 것 page!=10 && 
                                <>
                                    <Pagination.Next />
                                    <Pagination.Last />
                                </>
                            }
                        </Pagination>
                        */}
                    </Card.Body>
                </Card>
            </Container>
        </div>
    );
};

export default MyPage;