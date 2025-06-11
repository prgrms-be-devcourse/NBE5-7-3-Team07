import React from 'react';

export function Input({ className, ...props }) {
  return (
    <input
      className={`input ${className || ''}`}
      {...props}
    />
  );
}
