export function UserProfile({nickname, label}) {
  return (
      <div>
        <p className="text-sm text-muted">{label}</p>
        <div className="flex items-center mt-1 space-x-2">
          {nickname ? (
              <img src={"/placeholder.svg"} alt={nickname}
                   className="w-8 h-8 rounded-full"/>
          ) : (
              <div
                  className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center">
                {nickname.charAt(0) || "?"}
              </div>
          )}
          <span className="font-medium">{nickname || "알 수 없음"}</span>
        </div>
      </div>
  )
}
