"use client"
import React from 'react';

export function Checkbox({ className, checked, onCheckedChange, disabled, ...props }) {
  const checkboxClass = [
    'checkbox',
    checked ? 'checked' : '',
    disabled ? 'disabled' : '',
    className || ''
  ].filter(Boolean).join(' ');

  return (
    <div
      className={checkboxClass}
      onClick={() => !disabled && onCheckedChange && onCheckedChange(!checked)}
      {...props}
    >
      {checked && (
        <svg 
          className="checkbox-icon" 
          xmlns="http://www.w3.org/2000/svg" 
          viewBox="0 0 24 24" 
          fill="none" 
          stroke="currentColor" 
          strokeWidth="2" 
          strokeLinecap="round" 
          strokeLinejoin="round"
        >
          <polyline points="20 6 9 17 4 12" />
        </svg>
      )}
    </div>
  );
}
