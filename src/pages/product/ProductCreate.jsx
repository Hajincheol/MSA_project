import React, { useState } from 'react';
import { Button, Card, Container, Form } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const ProductCreate = () => {

    const navi = useNavigate();         // navigate
    const[data, setData] = useState({   // product
        'name': '',
        'category': '',
        'price': '',
        'stockQuantity': ''
    });

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

    // 제품 등록
    const productRegister = async(e) => {

        e.preventDefault();

        if(!(parseInt(data.price) > 0 && parseInt(data.stockQuantity) > 0)) {
            alert("가격 또는 수량이 0보다 커야 합니다.");
            return;
        }

        try {
            const res = await fetch(`http://localhost:8081/product/save`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-type": "application/json;charset=utf-8",
                    "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
                },
                body: JSON.stringify(data)
            });

            if(res.ok) {
                alert("성공적으로 등록되었습니다.");
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
            } else {
                alert("통신 에러 발생");
            }
            
        } catch {
            alert("통신 에러 발생");
        }
    }
    
    return (
        <div>
            <br />
            <br />
            <Container>
                <Card>
                    <Card.Body>
                        <Card.Title className="text-center">제품 등록</Card.Title>

                        <Form onSubmit={productRegister}>
                            
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>이름</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="name"
                                    placeholder="제품 이름을 입력해 주세요."
                                    onChange={(e) => setData({...data, [e.target.name]: e.target.value})}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>분류</Form.Label>
                                <Form.Select
                                    aria-label="Default select example"
                                    name="category"
                                    onChange={(e) => setData({...data, [e.target.name]: e.target.value})}
                                    required
                                >
                                    <option hidden>분류를 골라주세요.</option>
                                    <option value="전자제품">전자제품</option>
                                    <option value="의류">의류</option>
                                    <option value="식품">식품</option>
                                    <option value="생활용품">생활용품</option>
                                    <option value="기타">기타</option>
                                </Form.Select>
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>가격</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="price"
                                    placeholder="제품 가격을 입력해 주세요."
                                    onChange={(e) => setData({...data, [e.target.name]: e.target.value})}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>수량</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="stockQuantity"
                                    placeholder="제품 수량을 입력해 주세요."
                                    onChange={(e) => setData({...data, [e.target.name]: e.target.value})}
                                    required
                                />
                            </Form.Group>

                            <Button variant="primary" type="submit">등록</Button>
                            {' '}
                            <Button variant="danger" type="reset">초기화</Button>
                        </Form>
                    </Card.Body>
                </Card>
            </Container>
        </div>
    );
};

export default ProductCreate;