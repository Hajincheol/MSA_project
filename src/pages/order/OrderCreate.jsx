import React, { useEffect, useState } from 'react';
import { Button, Card, Container, Pagination } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';

const OrderCreate = (props) => {

    // navigate, product id, product, order
    const navi = useNavigate();
    const p_id = useParams().p_id;
    const[product, setProduct] = useState({
        'name': '',
        'category': '',
        'price': '',
        'stockQuantity': ''
    });
    const[order, setOrder] = useState({
        'productId': `${p_id}`,
        'productCount': 1
    });
    
    // product id로 product 정보 가져오기
    useEffect(() => {

        fetch(`http://localhost:8081/product/selectById/` + p_id, {
            method: "POST",
            credentials: "include",
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
            }
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
                    localStorage.removeItem("id");

                    navi("/");
                }

                return null;

            } else {
                alert("통신 에러");
                return null;
            }
        })
        .then((res) => {

            if(res !== null) {
                setProduct({...product, ...res});
            }
        })
        .catch((err) => console.log(err));
    }, []);

    // 만료된 accessToken을 새로 발급하기
    const newAccessToken = async() => {

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

    const createOrdering = async() => {

        if(order.productCount > product.stockQuantity) {
            alert("실제 상품 수량보다 많이 구매할 수 없습니다.");
            return;
        }

        try {
            const res = await fetch(`http://localhost:8081/ordering/create`, {
                method: "POST",
                headers: {
                    "Content-type": "application/json;charset=utf-8",
                    "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
                },
                credentials: "include",
                body: JSON.stringify(order)
            });

            if(res.ok) {
                alert('성공적으로 상품을 구매하셨습니다.');
                navi("/product/list");

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

                return null;

            } else {
                alert("통신 에러");
                return null;
            }

        } catch {
            alert("통신 실패");
        }
    }

    return (
        <div>
            <br />
            <br />
            <Container>
                <Card>
                    <Card.Header>제품 구매</Card.Header>
                    <Card.Body>
                        <Card.Text>이름 : {product.name}</Card.Text>
                        <Card.Text>분류 : {product.category}</Card.Text>
                        <Card.Text>가격 : {product.price}</Card.Text>
                        <div className="d-flex align-items-center">
                            <Card.Text className='me-2'>수량 :</Card.Text>
                            <Pagination>
                                {order.productCount > 1
                                ?
                                    <Pagination.Prev onClick={() => setOrder({...order, 'productCount': order.productCount-1})} />
                                :
                                    <Pagination.Prev disabled />
                                }


                                <Pagination.Item active >{order.productCount}</Pagination.Item>


                                {order.productCount<product.stockQuantity
                                ?
                                    <Pagination.Next onClick={() => setOrder({...order, 'productCount': order.productCount+1})}/>
                                :
                                    <Pagination.Next disabled />
                                }
                            </Pagination>
                        </div>
                            
                        <Button variant="primary" onClick={createOrdering}>구매</Button>
                    </Card.Body>
                </Card>
            </Container>
        </div>
    );
};

export default OrderCreate;