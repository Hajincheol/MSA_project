import React from 'react';

const Footer = () => {
    return (
        <footer
            className='bg-dark
                        py-3
                        mt-auto
                        d-flex
                        align-items-center
                        justify-content-center'
            style={{height: "100px"}}
        >
            {/*
                d-flex  => display: flex;
                        => flex 방식으로 표시
                        => 여러 요소를 원하는 방향과 규칙으로 배치하기 위한 레이아웃 시스템
                        => 자식 요소들의 배치 관계를 한꺼번에 제어 가능

                py-3    => 상하 padding 1rem

                mt-auto => display: "flex"일 경우
                        => margin-top: auto
                        => 남는 공간을 위쪽 margin으로 모두 위쪽 요소가 가져가게 해서 해당 요소를 아래쪽으로 밀어내게 만듬

                align-items-center      => 가운데 정렬
                justify-content-center  => 요소 가로 방향 가운데 정렬
            */}
            <p>
                <a
                    className="text-white text-decoration-none"
                    href='https://github.com/Hajincheol/MSA_project'
                >
                    코드
                </a>
            </p>
            <p className="text-white"> © 2026 React Bootstrap</p>
        </footer>
    );
};

export default Footer;