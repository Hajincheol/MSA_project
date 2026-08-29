import React, { useState } from 'react';
import { Button, Card, Container, Form } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const Login = () => {

    const navi = useNavigate();
    const[email, setEmail] = useState('');
    const[password, setPassword] = useState('');

    const loginSubmit = async(e) => {

        e.preventDefault();

        try {
            const res = await fetch(`http://localhost:8081/member/login`, {
                method: "POST",
                headers: {
                    "Content-type": "application/json;charset=utf-8"
                },
                credentials: "include",
                body: JSON.stringify({email, password})
            });

            if(!res.ok) {
                console.log("통신 실패");
                return;
            }

            const data = await res.json();

            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);
            localStorage.setItem("id", data.id);

            navi("/member/mypage");
        } catch {
            alert("로그인 실패");
        }
    }

    return (
        <div>
            <br />
            <br />
            <Container>
                <Card>
                    <Card.Body>
                        <Card.Title className="text-center">로그인</Card.Title>
                        
                        <Form onSubmit={loginSubmit}>
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>이메일</Form.Label>
                                <Form.Control
                                    type="email" 
                                    placeholder="이메일을 입력해 주세요." 
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>비밀번호</Form.Label>
                                <Form.Control
                                    type="password" 
                                    placeholder="비밀번호를 입력해 주세요." 
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </Form.Group>

                            <Button variant="primary" type="submit">로그인</Button>
                            {' '}
                            <Button variant="danger" type="reset">초기화</Button>
                        </Form>
                    </Card.Body>
                </Card>
            </Container>
        </div>
    );
};

export default Login;