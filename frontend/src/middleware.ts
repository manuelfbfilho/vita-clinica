import { NextRequest, NextResponse } from "next/server";

const PUBLIC_PATHS = ["/login", "/cadastro"];

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  // Permite rotas públicas
  if (PUBLIC_PATHS.some((p) => pathname.startsWith(p))) {
    return NextResponse.next();
  }

  // Verifica token no cookie
  const token = req.cookies.get("vita-token")?.value;
  if (!token) {
    return NextResponse.redirect(new URL("/login", req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|api).*)"],
};
