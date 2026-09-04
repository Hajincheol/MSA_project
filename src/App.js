import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Header from './pages/common/Header';
import SignUp from './pages/member/SignUp';
import Login from './pages/member/Login';
import MyPage from './pages/member/MyPage';
import ProductList from './pages/product/ProductList';
import ProductUpdate from './pages/product/ProductUpdate';
import OrderCreate from './pages/order/OrderCreate';
import ProductCreate from './pages/product/ProductCreate';
import Footer from './pages/common/Footer';

function App() {
  return (
    <div className="d-flex flex-column min-vh-100">
      {/*
          => footer를 위한 선언

          d-flex      => display: flex;
                      => flex 방식으로 표시
                      => 여러 요소를 원하는 방향과 규칙으로 배치하기 위한 레이아웃 시스템
                      => 자식 요소들의 배치 관계를 한꺼번에 제어 가능

          flex-column => flex-direction: column;
                      => flex 방향을 세로 방향을 변경
                      => 기본이 가로 방향

          min-vh-100  => min-height : 100vh;
                      => vh는 브라우저 화면 높이
                      => 100vh는 화면 높이 100%
                      => 즉, 화면 높이가 최소 브라우저 화면을 화면 전체 높이만큼 차지
      */}
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
          
          <Route path="/order/create/:p_id" element={<OrderCreate />} />
        </Routes>
        
        <Footer />
      </BrowserRouter>
    </div>
  );
}

export default App;
