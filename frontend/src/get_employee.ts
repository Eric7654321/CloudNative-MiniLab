export const RoleToString = (role: number): string => {
  switch(role) {
    case 0:
      return "員工"
    case 1:
      return "管理員"
    default:
      return ""
  }
}
