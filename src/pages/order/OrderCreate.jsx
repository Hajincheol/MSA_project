import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const OrderCreate = () => {

    const navi = useNavigate();
    const[order, setOrder] = useState({
        'productId': '',
        'productCount': ''
    });

    useEffect(() => {

    }, []);

    const createOrdering = async(e) => {

        e.preventDefault();

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

            if(!res.ok) {
                alert("통신 중 에러 발생");
            } else {

                const data = await res.json();

                alert(`${data}이 주문 되었습니다.`);
                navi("/product/list");
            }

        } catch {
            alert("통신 실패");
        }
    }

    return (
        <div>
            
        </div>
    );
};

export default OrderCreate;