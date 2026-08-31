import React from 'react';
import { Container, Nav, Navbar } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';

const Header = () => {

    const condition = localStorage.getItem("accessToken");  // accessToken 여부
    const navi = useNavigate();                             // navigate

    /* 만료된 accessToken을 새로 발급하기
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
    */

    // 로그아웃
    const handleLogout = async() => {

        try {
            const res = await fetch(`http://localhost:8081/member/logout`, {
                method: "POST",
                headers: {
                    "Content-type": "application/json;charset=utf-8"
                },
                credentials: "include",
                body: JSON.stringify({ 'refreshToken' : localStorage.getItem("refreshToken") })
            });

            if(!res.ok) console.log("로그아웃 문제 발생");

            // token 삭제
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            localStorage.removeItem("id");

            // header 로그아웃 상태로 전환
            navi("/");

        } catch {
            alert("로그아웃 에러 발생");
        }
    }

    return (
        <Navbar expand="lg" bg="dark" variant="dark">
            <Container>
                <Navbar.Brand href="#">제품관리</Navbar.Brand>
                <Navbar.Toggle aria-controls="navbarScroll"/>
                <Navbar.Collapse id="navbarScroll">
                    <Nav
                        className="me-auto my-2 my-lg-0"
                        navbarScroll
                    >

                        {condition!==null ?
                        (
                            <>
                                <Link to="/product/create" className="nav-link">제품등록</Link>
                                <Link to="/product/list" className="nav-link">제품목록</Link>
                                <Link to="/member/mypage" className="nav-link">마이페이지</Link>
                                <Link onClick={handleLogout} className="nav-link">로그아웃</Link>
                            </>
                        )
                        :
                        (
                            <>
                                <Link to="/member/signup" className="nav-link">회원가입</Link>
                                <Link to="/member/login" className="nav-link">로그인</Link>
                                <Link to="/product/list" className="nav-link">제품목록</Link>
                            </>
                        )
                        }

                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default Header;