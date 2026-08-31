import React, { useEffect, useState } from 'react';
import { Container } from 'react-bootstrap';
import ProductItem from './ProductItem';

const ProductList = () => {

    const[pList, setPList] = useState([]);

    useEffect(() => {

        fetch(`http://localhost:8081/product/list`, {
            method: "POST"
        })
        .then((res) => {
            if(res.ok) {
                return res.json();
            } else {
                return null;
            }
        })
        .then((res) => {

            if(res !== null) {
                setPList(res);
            }

        })
        .catch((err) => console.log(err));
    }, []);

    return (
        <div>
            <Container>
                <br />
                <h2 className='text-center'>제품 목록</h2>
                <br />
        
                {pList.length > 0 && pList.map((product) => <ProductItem key={product.id} product={product} />)}
            </Container>
            <br />
            <br />
        </div>
    );
};

export default ProductList;