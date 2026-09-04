import React, { useEffect, useState } from 'react';
import { Button, Card, Container, Form, Pagination, Tab, Tabs } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const MyPage = () => {

    // navigate
    const navi = useNavigate();

    // 등록 or 주문 상품 list / page / 등록or주문 탭 여부
    const[page, setPage] = useState(1);
    const[pList, setPList] = useState([]);
    const[oList, setOList] = useState([]);
    const[key, setKey] = useState('');

    // 내 정보
    const[myinfo, setMyinfo] = useState({
        'name': '',
        'email': ''
    });

    useEffect(() => {

        setKey('home');
        myInfoUpdate();
        myProductList();
        myOrderList();

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

    // 내 상품 목록 가져오기
    const myProductList = () => {

        fetch(`http://localhost:8081/product/selectByMemberId/${localStorage.getItem("id")}`, {
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

                // 인증 에러(401)가 발생시 accessToken이 만료되었다는 에러이니 재발급
                if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                    newAccessToken();

                } else {

                    alert("로그아웃 되셨습니다.");

                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");
                    localStorage.removeItem("id");

                    navi("/");
                }
            } else {
                return null;
            }
        })
        .then((res) => {

            if(res !== null) {

                setPList(res);
            }
        })
        .catch((err) => alert("product list 에러 발생", err));
    }

    // 내 주문 목록 가져오기
    const myOrderList = () => {

        fetch(`http://localhost:8081/ordering/selectByMemberId/${localStorage.getItem("id")}`, {
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

                // 인증 에러(401)가 발생시 accessToken이 만료되었다는 에러이니 재발급
                if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                    newAccessToken();

                } else {

                    alert("로그아웃 되셨습니다.");

                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");
                    localStorage.removeItem("id");

                    navi("/");
                }

            } else {
                return null;
            }
        })
        .then((res) => {

            if(res !== null) {

                setOList(res);
            }
        })
        .catch((err) => alert("order list 에러 발생", err));
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
                
                // 인증 에러(401)가 발생시 accessToken이 만료되었다는 에러이니 재발급
                if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                    newAccessToken();

                } else {

                    alert("로그아웃 되셨습니다.");

                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");
                    localStorage.removeItem("id");

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
            const res = await fetch(`http://localhost:8081/member/mypage/nameChange/` + myinfo.name, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
                },
                credentials: "include"
            });

            if(res.ok) {
                const data = await res.json();

                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);

                myInfoUpdate();

            } else if(res.status === 401) {
                
                // 인증 에러(401)가 발생시 accessToken이 만료되었다는 에러이니 재발급
                if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                    newAccessToken();

                } else {

                    alert("로그아웃 되셨습니다.");

                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");
                    localStorage.removeItem("id");

                    navi("/");
                }

            } else {
                alert("통신 에러");
            }


        } catch {
            console.log("통신 실패");
        }
    }

    // 주문 취소
    const orderCancel = async() => {

        if(window.confirm("정말 취소 하시겠습니까?")) {
            try {
                const res = await fetch(`http://localhost:8081/ordering/cancel/${oList[page-1].id}`, {
                    method: "POST",
                    headers: {
                        "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
                    },
                    credentials: "include"
                });

                if(res.ok) {
                    alert("취소 되셨습니다.");

                    myOrderList();
                    
                } else if(res.status === 401) {

                    // 인증 에러(401)가 발생시 accessToken이 만료되었다는 에러이니 재발급
                    if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                        newAccessToken();

                    } else {

                        alert("로그아웃 되셨습니다.");

                        localStorage.removeItem("accessToken");
                        localStorage.removeItem("refreshToken");
                        localStorage.removeItem("id");

                        navi("/");
                    }

                } else {
                    alert("통신 에러");
                }


            } catch {
                console.log("통신 실패");
            }
        }

    }

    return (
        <div>
            <br />
            <br />
            <Container>
                <Card>
                    <Card.Body>
                        {/* onSelect 다른 Tab이 선택되면 실행되는 속성 */}
                        {/* activeKey 현재 탭 설정 */}
                        <Tabs
                            defaultActiveKey="home"
                            id="fill-tab-example"
                            className="mb-3"
                            activeKey={key}
                            onSelect={(k) => {
                                setPage(1);
                                setKey(k);
                            }}
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
                                            onChange={(e) => setMyinfo({...myinfo, 'name': e.target.value})}
                                            required
                                        />
                                    </Form.Group>

                                    <Button variant="primary" type="submit">변경</Button>
                                    {' '}
                                    <Button variant="danger" type="reset">초기화</Button>
                                </Form>
                            </Tab>


                            <Tab eventKey="product" title="등록한 상품">

                                {pList.length > 0 && key === 'product'
                                ?
                                    <>
                                        <Card.Text>번호 : {pList[page-1].id}</Card.Text>
                                        <Card.Text>이름 : {pList[page-1].name}</Card.Text>
                                        <Card.Text>분류 : {pList[page-1].category}</Card.Text>
                                        <Card.Text>가격 : {pList[page-1].price}</Card.Text>
                                        <Card.Text>수량 : {pList[page-1].stockQuantity}</Card.Text>

                                        <Pagination className='d-flex justify-content-center'>
                                            {page > 2 ? <Pagination.First onClick={() => setPage(1)} /> : <Pagination.First disabled/>}
                                            {page > 1 ? <Pagination.Prev onClick={() => setPage(page-1)} /> : <Pagination.Prev disabled/>}
                                            {page-2 > 0 && <Pagination.Item onClick={() => setPage(page-2)}>{page-2}</Pagination.Item>}
                                            {page-1 > 0 && <Pagination.Item onClick={() => setPage(page-1)}>{page-1}</Pagination.Item>}


                                            <Pagination.Item active>{page}</Pagination.Item>


                                            {page < pList.length && <Pagination.Item onClick={() => setPage(page+1)}>{page+1}</Pagination.Item>}
                                            {page < pList.length-1 && <Pagination.Item onClick={() => setPage(page+2)}>{page+2}</Pagination.Item>}
                                            {page < pList.length ? <Pagination.Next onClick={() => setPage(page+1)} /> : <Pagination.Next disabled/>}
                                            {page < pList.length-1 ? <Pagination.Last onClick={() => setPage(pList.length)} /> : <Pagination.Last disabled/>}
                                        </Pagination>
                                    </>
                                :
                                    <>
                                        <br />
                                        <Card.Text className='text-center'>등록한 제품이 없습니다.</Card.Text>
                                        <br />
                                    </>
                                }
                            </Tab>


                            <Tab eventKey="order" title="주문한 상품">

                                {oList.length > 0 && key === 'order'
                                ?
                                    <>
                                        <Card.Text>번호 : {oList[page-1].id}</Card.Text>
                                        <Card.Text>이름 : {oList[page-1].product.name}</Card.Text>
                                        <Card.Text>수량 : {oList[page-1].quantity}</Card.Text>
                                        <Card.Text>상태 : {oList[page-1].orderStatus}</Card.Text>

                                        {oList[page-1].orderStatus !== 'CANCELED'
                                        &&
                                        <>
                                            <Button onClick={orderCancel} variant='primary'>주문 취소</Button>
                                            <br />
                                            <br />
                                        </>
                                        }

                                        <Pagination className='d-flex justify-content-center'>
                                            {page > 2 ? <Pagination.First onClick={() => setPage(1)} /> : <Pagination.First disabled/>}
                                            {page > 1 ? <Pagination.Prev onClick={() => setPage(page-1)} /> : <Pagination.Prev disabled/>}
                                            {page-2 > 0 && <Pagination.Item onClick={() => setPage(page-2)}>{page-2}</Pagination.Item>}
                                            {page-1 > 0 && <Pagination.Item onClick={() => setPage(page-1)}>{page-1}</Pagination.Item>}


                                            <Pagination.Item active>{page}</Pagination.Item>


                                            {page < oList.length && <Pagination.Item onClick={() => setPage(page+1)}>{page+1}</Pagination.Item>}
                                            {page < oList.length-1 && <Pagination.Item onClick={() => setPage(page+2)}>{page+2}</Pagination.Item>}
                                            {page < oList.length ? <Pagination.Next onClick={() => setPage(page+1)} /> : <Pagination.Next disabled/>}
                                            {page < oList.length-1 ? <Pagination.Last onClick={() => setPage(oList.length)} /> : <Pagination.Last disabled/>}
                                        </Pagination>
                                    </>
                                :
                                    <>
                                        <br />
                                        <Card.Text className='text-center'>주문한 제품이 없습니다.</Card.Text>
                                        <br />
                                    </>
                                }
                            </Tab>
                        </Tabs>
                    </Card.Body>
                </Card>
            </Container>
        </div>
    );
};

export default MyPage;