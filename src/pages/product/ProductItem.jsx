import React from 'react';
import { Button, Card } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const ProductItem = (props) => {

    const { id, name, category, price, stockQuantity, memberId } = props.product;

    const productUpdate = () => {

    }

    return (
        <div>
            <Card>
                <Card.Body>
                    <Card.Title>No.{id}</Card.Title>
                    <Card.Text>이름 : {name}</Card.Text>
                    <Card.Text>분류 : {category}</Card.Text>
                    <Card.Text>가격 : {price}</Card.Text>
                    <Card.Text>수량 : {stockQuantity}</Card.Text>

                    <Button variant='primary' onClick={productUpdate()}>수정</Button>
                </Card.Body>
            </Card>
        </div>
    );
};

export default ProductItem;