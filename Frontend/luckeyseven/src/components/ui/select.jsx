"use client"

import React, { useState, useRef, useEffect } from "react"

export function Select({ children, value, onValueChange, disabled, placeholder }) {
  const [isOpen, setIsOpen] = useState(false)
  const ref = useRef(null)

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (ref.current && !ref.current.contains(event.target)) {
        setIsOpen(false)
      }
    }

    document.addEventListener("mousedown", handleClickOutside)
    return () => {
      document.removeEventListener("mousedown", handleClickOutside)
    }
  }, [])

  return (
    <div ref={ref} className="select-container">
      <SelectTrigger onClick={() => !disabled && setIsOpen(!isOpen)} disabled={disabled} isOpen={isOpen}>
        <SelectValue placeholder={placeholder}>
          {React.Children.toArray(children)
            .filter((child) => React.isValidElement(child) && child.props.value === value)
            .map((child) => (React.isValidElement(child) ? child.props.children : null))}
        </SelectValue>
      </SelectTrigger>
      {isOpen && (
        <SelectContent>
          {React.Children.map(children, (child) => {
            if (React.isValidElement(child)) {
              return React.cloneElement(child, {
                onClick: () => {
                  onValueChange(child.props.value)
                  setIsOpen(false)
                },
              })
            }
            return child
          })}
        </SelectContent>
      )}
    </div>
  )
}

export function SelectTrigger({ children, onClick, disabled, isOpen, className, ...props }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`select-trigger ${isOpen ? 'open' : ''} ${className || ''}`}
      {...props}
    >
      {children}
      <svg
        className="h-4 w-4 opacity-50"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <polyline points="6 9 12 15 18 9" />
      </svg>
    </button>
  )
}

export function SelectValue({ children, placeholder }) {
  return <span className="select-value">{children || placeholder}</span>
}

export function SelectContent({ children, className, ...props }) {
  return (
    <div
      className={`select-content ${className || ''}`}
      {...props}
    >
      <div className="select-items-container">{children}</div>
    </div>
  )
}

export function SelectItem({ children, className, onClick, ...props }) {
  return (
    <div
      className={`select-item ${className || ''}`}
      onClick={onClick}
      {...props}
    >
      {children}
    </div>
  )
}
