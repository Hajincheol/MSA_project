import React, { useEffect, useState } from 'react';
import { Container } from 'react-bootstrap';
import ProductItem from './ProductItem';

const ProductList = () => {

    const[pList, setPList] = useState([]);

    useEffect(() => {

        fetch(`http://localhost:8081/product/list`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
            }
        })
        .then((res) => res.json())
        .then((res) => {
            console.log(res);
            setPList(res);
        })
        .catch((err) => console.log(err));
    }, []);

    return (
        <div>
            <Container>
                <br />
                <h2>제품 목록</h2>
                <br />
        
                {pList.map((product) => <ProductItem key={product.id} product={product} />)}
            </Container>
        </div>
    );
};

export default ProductList;