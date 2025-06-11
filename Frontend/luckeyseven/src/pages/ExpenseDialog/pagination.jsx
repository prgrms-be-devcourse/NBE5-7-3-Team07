import React from 'react';


export default function pagination({ currentPage, totalPages, onPageChange }) {
  const pageNumbers = [];
  for (let i = 1; i <= totalPages; i++) {
    pageNumbers.push(i);
  }

  return (
    <nav className="pagination-container">
      <button
        className="page-btn"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
      >
        이전
      </button>

      {pageNumbers.map((number) => (
        <button
          key={number}
          className={`page-btn ${number === currentPage ? 'active' : ''}`}
          onClick={() => onPageChange(number)}
        >
          {number}
        </button>
      ))}

      <button
        className="page-btn"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
      >
        다음
      </button>
    </nav>
  );
}