import React, { useState } from 'react';
import { Button, Card, Container, Form } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const SignUp = () => {

    const navi = useNavigate();
    const[data, setData] = useState({
        'name': '',
        'email': '',
        'password': ''
    });

    const changeValue = (e) => {
        setData({
            ...data,
            [e.target.name]: e.target.value
        });
    }

    const memberSignup = async(e) => {

        e.preventDefault();

        if(data.password.length < 4) {
            alert("비밀번호를 4자리 이상 입력해 주세요.");
            return;
        }

        try {
            const res = await fetch(`http://localhost:8081/member/create`, {
                method: "POST",
                headers: {
                    "Content-type": "application/json;charset=utf-8"
                },
                credentials: "include",
                body: JSON.stringify(data)
            });

            if(!res.ok) {
                alert("통신 실패");
                return;
            }

            alert("회원가입 성공");
            navi("/");

        } catch {
            alert('회원가입 실패');
        }
    }

    return (
        <div>
            <br />
            <br />
            <Container>
                <Card>
                    <Card.Body>
                        <Card.Title className="text-center">회원가입</Card.Title>

                        <Form onSubmit={memberSignup}>
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>이름</Form.Label>
                                <Form.Control
                                    name="name"
                                    type="text"
                                    placeholder="이름을 입력해 주세요."
                                    onChange={changeValue}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>이메일</Form.Label>
                                <Form.Control
                                    name="email"
                                    type="email"
                                    placeholder="이메일을 입력해 주세요."
                                    onChange={changeValue}
                                    required
                                />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>비밀번호</Form.Label>
                                <Form.Control
                                    name="password"
                                    type="password"
                                    placeholder="비밀번호을 입력해 주세요."
                                    onChange={changeValue}
                                    minLength={4}
                                    required
                                />
                            </Form.Group>

                            <Button variant="primary" type="submit">회원가입</Button>
                            {' '}
                            <Button variant="danger" type="reset">초기화</Button>
                        </Form>
                    </Card.Body>
                </Card>
            </Container>
        </div>
    );
};

export default SignUp;