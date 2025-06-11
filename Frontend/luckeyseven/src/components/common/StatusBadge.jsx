export function StatusBadge({ isSettled }) {
  return (
    <span className={`badge ${isSettled ? "badge-outline" : "badge-secondary"}`}>
      {isSettled ? "정산 완료" : "정산 대기중"}
    </span>
  )
}
