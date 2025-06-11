// 예시 사용자 데이터
export const users = [
  { id: "user1", name: "홍길동", avatar: "/placeholder.svg?height=32&width=32" },
  { id: "user2", name: "김철수", avatar: "/placeholder.svg?height=32&width=32" },
  { id: "user3", name: "이영희", avatar: "/placeholder.svg?height=32&width=32" },
  { id: "user4", name: "박지성", avatar: "/placeholder.svg?height=32&width=32" },
]

// 예시 지출 데이터
export const expenses = [
  { id: "expense1", title: "저녁 식사", amount: 50000, date: new Date().toISOString() },
  { id: "expense2", title: "택시비", amount: 15000, date: new Date().toISOString() },
  { id: "expense3", title: "숙박비", amount: 120000, date: new Date().toISOString() },
  { id: "expense4", title: "관광 티켓", amount: 30000, date: new Date().toISOString() },
]

// 예시 정산 데이터
export const settlements = [
  {
    id: "settlement1",
    amount: 25000,
    isSettled: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    payerId: "user1",
    settlerId: "user2",
    expenseId: "expense1",
    payer: users.find((u) => u.id === "user1"),
    settler: users.find((u) => u.id === "user2"),
    expense: expenses.find((e) => e.id === "expense1"),
  },
  {
    id: "settlement2",
    amount: 7500,
    isSettled: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    payerId: "user1",
    settlerId: "user3",
    expenseId: "expense2",
    payer: users.find((u) => u.id === "user1"),
    settler: users.find((u) => u.id === "user3"),
    expense: expenses.find((e) => e.id === "expense2"),
  },
  {
    id: "settlement3",
    amount: 40000,
    isSettled: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    payerId: "user4",
    settlerId: "user2",
    expenseId: "expense3",
    payer: users.find((u) => u.id === "user4"),
    settler: users.find((u) => u.id === "user2"),
    expense: expenses.find((e) => e.id === "expense3"),
  },
  {
    id: "settlement4",
    amount: 10000,
    isSettled: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    payerId: "user3",
    settlerId: "user1",
    expenseId: "expense4",
    payer: users.find((u) => u.id === "user3"),
    settler: users.find((u) => u.id === "user1"),
    expense: expenses.find((e) => e.id === "expense4"),
  },
]
