import React from 'react';
import { Button, Card } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';

const ProductItem = (props) => {

    // navigate, product, 참조된 memberid
    const navi = useNavigate();
    const { id, name, category, price, stockQuantity } = props.product;
    const memberId = `${props.product.member.id}`;

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
    
    // 제품 삭제
    const productDelete = async() => {
        
        try {
            const res = await fetch(`http://localhost:8081/product/delete/${id}`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
                }
            });

            if(res.ok) {
                alert("성공적으로 삭제되었습니다.");
                window.location.reload();
                
            } else if(res.status === 401) {

                // 인증 에러(401)가 발생시 accessToken이 만료되었다는 에러이니 재발급
                if(window.confirm("로그인 시간이 만료되었습니다. 연장하시겠습니까?")) {
                    newAccessToken();

                } else {
                    alert("로그아웃 되셨습니다.");

                    // localStorage에 저장된 데이터 삭제
                    localStorage.removeItem("accessToken");
                    localStorage.removeItem("refreshToken");
                    localStorage.removeItem("id");

                    navi("/");
                }
            } else {
                alert("통신 에러 발생");
            }
        } catch {
            alert("통신 실패");
        }
    }

    return (
        <div>
            <Card>
                <Card.Header>번호 : {id}</Card.Header>
                <Card.Body>
                    <Card.Text>이름 : {name}</Card.Text>
                    <Card.Text>분류 : {category}</Card.Text>
                    <Card.Text>가격 : {price}</Card.Text>
                    <Card.Text>수량 : {stockQuantity}</Card.Text>
                    
                    {/* 본인 일시 수정, 삭제 / 아닐 경우 구매 */
                    localStorage.getItem("id") === memberId
                    ?
                        <>
                            <Link to={"/product/update/" + id} className="btn btn-primary">수정</Link>
                            {' '}
                            <Button variant='danger' onClick={productDelete}>삭제</Button>
                        </>
                    :
                        <Link to={"/order/create/" + id} className="btn btn-primary">구매</Link>
                    }
                </Card.Body>
            </Card>
            <br />
        </div>
    );
};

export default ProductItem;