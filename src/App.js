import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Header from './pages/common/Header';
import SignUp from './pages/member/SignUp';
import Login from './pages/member/Login';
import MyPage from './pages/member/MyPage';
import ProductList from './pages/product/ProductList';
import ProductUpdate from './pages/product/ProductUpdate';
import OrderCreate from './pages/order/OrderCreate';
import ProductCreate from './pages/product/ProductCreate';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Header />
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/member/login" element={<Login />} />
          <Route path="/member/signup" element={<SignUp />} />
          <Route path="/member/mypage" element={<MyPage />} />

          <Route path="/product/create" element={<ProductCreate />} />
          <Route path="/product/list" element={<ProductList />} />
          <Route path="/product/update/:p_id" element={<ProductUpdate />} />
          
          <Route path="/ordering/create" element={<OrderCreate />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
